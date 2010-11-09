/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.jsr88.remote_1_2;

import java.io.InputStream;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * A container implementation for a JSR 88-compliant container.
 *
 * <p>This class distributes (deploys) and undeploys ShrinkWrap
 * archives using the JSR-88 {@link DeploymentManager}. A connection
 * to the container is established during the start method and
 * released in the stop method. The deploy and undeploy methods use
 * {@link DeploymentManager#distribute(Target[], ModuleType, InputStream, InputStream)} and
 * {@link DeploymentManager#undeploy(TargetModuleID[])}, respectively.</p>
 *
 * <p>You can use this container implementation either through configuration
 * or extension. To use it via configuration, assign the deployment URI and
 * deployment factory class for the target container to the {@link JSR88Configuration}
 * object using the Arquillian configuration file (arquillian.xml). Alternatively,
 * you can choose to extend the {@link JSR88Configuration} to assign defaults for
 * these values. You should also specify the configuration class by overriding the
 * {@link JSR88CompliantRemoteContainer#getConfigurationClass()}
 * method.</p>
 *
 * <p>JSR 88 deploys the archive using an {@link InputStream}. The deployed
 * archive is assigned a random name. You can specify a custom name for 
 * a war in the <code>module-name</code> element of the module deployment
 * descriptor (web.xml).</p>
 *
 * @author Dan Allen
 * @author Iskandar Salim
 * @see org.glassfish.admin.cli.resources.AddResources
 */
public class JSR88RemoteContainer<T extends JSR88Configuration> implements DeployableContainer<T>
{
   public static final String HTTP_PROTOCOL = "http";
   public static final ArchivePath MODULE_ID_STORE_PATH = ArchivePaths.create(".jsr88-module-id");

   private static final CyclicBarrier PROGRESS_BARRIER = new CyclicBarrier(2);

   private static final Logger log = Logger.getLogger(JSR88RemoteContainer.class.getName());

   private JSR88ModuleTypeMapper moduleTypeMapper;
   private DeploymentManager deploymentManager;
   private boolean moduleStarted = false;

   private T containerConfig;
   
   public JSR88RemoteContainer()
   {
      moduleTypeMapper = new JSR88ModuleTypeMapper();
   }
   
   public void setup(T containerConfig)
   {
      this.containerConfig = containerConfig;
   }
   
   public void start() throws LifecycleException
   {
      try 
      {
         initDeploymentManager(containerConfig.getDeploymentFactoryClass(),
               containerConfig.getDeploymentUri(),
               containerConfig.getDeploymentUsername(),
               containerConfig.getDeploymentPassword());
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not connect to container", e);
      }
   }

   public void stop() throws LifecycleException
   {
      try
      {
         releaseDeploymentManager();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not release deployment manager", e);
      }
   }

   public ProtocolMetaData deploy(Deployment... deployments) throws DeploymentException
   {
      if (deploymentManager == null)
      {
         throw new DeploymentException("Could not deploy since deployment manager was not loaded");
      }

      TargetModuleID moduleInfo = null;
      try 
      {
         for(Deployment deployment : deployments)
         {
            if(deployment.isArchiveDeployment())
            {
               PROGRESS_BARRIER.reset();
               resetModuleStatus();
               ProgressObject progress = deploymentManager.distribute(
                     deploymentManager.getTargets(), moduleTypeMapper.getModuleType(deployment.getArchive()),
                     deployment.getArchive().as(ZipExporter.class).exportAsInputStream(), null);
               progress.addProgressListener(new JSR88DeploymentListener(this, progress.getResultTargetModuleIDs(), CommandType.DISTRIBUTE));
               waitForModuleToStart();
               // QUESTION when is getResultTargetModuleIDs() > 0?
               moduleInfo =  progress.getResultTargetModuleIDs()[0];
               
               context.add(TargetModuleID.class, moduleInfo);
            }
         }
         
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not deploy archive", e);
      }

      if (moduleInfo == null || moduleInfo.getModuleID() == null)
      {
         throw new DeploymentException("Could not determine module id, likely because module did not deploy");
      }

      try 
      {
         // FIXME pass moduleId to ServletMethodExecutor since we can't guarantee anymore it's /test
         return new ProtocolMetaData().addContext(
               new HTTPContext(containerConfig.getRemoteServerAddress(), containerConfig.getRemoteServerHttpPort(), "/test"));
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not create ContainerMethodExecutor", e);
      }
   }

   public void undeploy(Deployment... deployments) throws DeploymentException
   {
      if (!moduleStarted)
      {
         log.info("Skipping undeploy since module is not deployed");
         return;
      }

      if (deploymentManager == null)
      {
         throw new DeploymentException("Could not undeploy since deployment manager was not loaded");
      }
      
      for(Deployment deployment : deployments)
      {
         if(deployment.isArchiveDeployment())
         {
            try
            {
               PROGRESS_BARRIER.reset();
               TargetModuleID moduleInfo = context.get(TargetModuleID.class);
               if (moduleInfo == null || moduleInfo.getModuleID() == null)
               {
                  log.log(Level.INFO, "Skipping undeploy since module ID could not be determined");
                  return;
               }
               
               TargetModuleID[] availableModuleIDs = deploymentManager.getAvailableModules(
                     moduleTypeMapper.getModuleType(deployment.getArchive()), getDeploymentManager().getTargets());
               TargetModuleID moduleInfoMatch = null;
               for (TargetModuleID candidate : availableModuleIDs)
               {
                  if (candidate.getModuleID().equals(moduleInfo.getModuleID()))
                  {
                     moduleInfoMatch = candidate;
                     break;
                  }
               }
   
               if (moduleInfoMatch != null)
               {
                  TargetModuleID[] targetModuleIDs = { moduleInfoMatch };
                  ProgressObject progress = deploymentManager.undeploy(targetModuleIDs);
                  progress.addProgressListener(new JSR88DeploymentListener(this, targetModuleIDs, CommandType.UNDEPLOY));
                  waitForModuleToUndeploy();
               }
               else
               {
                  resetModuleStatus();
                  log.info("Undeploy skipped since could not locate module in list of deployed modules");
               }
            }
            catch (Exception e)
            {
               throw new DeploymentException("Could not undeploy module", e);
            }
         }
      }
   }

   public Class<T> getConfigurationClass()
   {
      return JSR88Configuration.class;
   }

   public DeploymentManager getDeploymentManager()
   {
      return deploymentManager;
   }

   protected DeploymentManager initDeploymentManager(String factoryClass, String uri, String username, String password) throws Exception
   {
      if (deploymentManager == null)
      {
         DeploymentFactoryManager dfm = DeploymentFactoryManager.getInstance();
         dfm.registerDeploymentFactory(
               (DeploymentFactory) Class.forName(factoryClass).newInstance());
         deploymentManager =
               dfm.getDeploymentManager(uri, username, password);
      }
      return deploymentManager;
   }

   protected void releaseDeploymentManager()
   {
      if (deploymentManager != null)
      {
         deploymentManager.release();
      }
   }

   public boolean isModuleStarted()
   {
      return moduleStarted;
   }

   void moduleStarted(boolean status)
   {
      moduleStarted = status;
      if (PROGRESS_BARRIER.getNumberWaiting() > 0)
      {
         try
         {
            PROGRESS_BARRIER.await();
         }
         catch (Exception e)
         {
            throw new RuntimeException("Failed to report module as " + (status ? "started" : "shutdown"), e);
         }
      }
   }

   protected void resetModuleStatus()
   {
      moduleStarted = false;
   }

   private void waitForModuleToStart()
   {
      if (!moduleStarted)
      {
         try
         {
            PROGRESS_BARRIER.await(containerConfig.getDeploymentTimeoutSeconds(), TimeUnit.SECONDS);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Module startup was interrupted or timed out", e);
         }
      }
   }

   private void waitForModuleToUndeploy()
   {
      if (moduleStarted)
      {
         try
         {
            PROGRESS_BARRIER.await(containerConfig.getDeploymentTimeoutSeconds(), TimeUnit.SECONDS);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Module undeployment was interrupted or timed out", e);
         }
      }
   }
}
