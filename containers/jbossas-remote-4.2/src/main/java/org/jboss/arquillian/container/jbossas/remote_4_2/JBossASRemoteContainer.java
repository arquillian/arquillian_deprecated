/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.jbossas.remote_4_2;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.MBeanServerConnection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.deployment.spi.DeploymentManagerImpl;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * JBossASRemoteContainer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author Davide D'Alto
 * @version $Revision: $
 */
public class JBossASRemoteContainer implements DeployableContainer<JBossASConfiguration>
{
   private static final TargetModuleID[] EMPTY_ARRAY = new TargetModuleID[0];
   
   private JBossASConfiguration configuration;

   private DeploymentManager deploymentManager;
   
   private Context context;
   
   private MBeanServerConnection serverConnection;
   
   private TargetModuleID[] targetModuleIDs = EMPTY_ARRAY;

   @Override
   public Class<JBossASConfiguration> getConfigurationClass()
   {
      return JBossASConfiguration.class;
   }

   @Override
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 2.5");
   }
   
   @Override
   public void setup(JBossASConfiguration configuration)
   {
      this.configuration = configuration;
   }

   @Override
   public void start() throws LifecycleException
   {
      try
      {
         createContext();
         deploymentManager = createDeploymentManager();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not connect to container", e);
      }
   }
   
   private Context createContext() throws Exception
   {
      if (context == null)
      {
         Properties props = new Properties();
         props.put(InitialContext.INITIAL_CONTEXT_FACTORY, configuration.getContextFactory());
         props.put(InitialContext.URL_PKG_PREFIXES, configuration.getUrlPkgPrefix());
         props.put(InitialContext.PROVIDER_URL, configuration.getProviderUrl());
         context = new InitialContext(props);
      }
      return context;
   }
   
   private DeploymentManager createDeploymentManager() throws DeploymentManagerCreationException
   {
      DeploymentFactoryManager dfm = DeploymentFactoryManager.getInstance();
      return dfm.getDeploymentManager(DeploymentManagerImpl.DEPLOYER_URI, null, null);
   }

   @Override
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      URL deploymentUrl = ShrinkWrapUtil.toURL(descriptor);
      deploy(new File(deploymentUrl.getFile()));
   }
 
   @Override
   public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
      {
         throw new IllegalArgumentException("Archive must be specified");
      }

      File deployment = ShrinkWrapUtil.toFile(archive);
      targetModuleIDs = deploy(deployment);

      try
      {
         return ManagementViewParser.parse(archive.getName(), getServerConnection());
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not extract deployment metadata", e);
      }
   }

   private TargetModuleID[] deploy(File deployment) throws DeploymentException
   {
      ProgressObject progress;
      try
      {
         File deploymentPlan = ShrinkWrapUtil.createDeploymentPlan(deployment);
         progress = deploymentManager.distribute(deploymentManager.getTargets(), deployment, deploymentPlan);
      }
      catch (IOException e)
      {
         throw new DeploymentException("Failed to deploy " + deployment.getName(), e);
      }
      DeploymentStatus status = progress.getDeploymentStatus();
      if (status.getState() == StateType.FAILED)
      {
         throw new DeploymentException("Failed to deploy " + deployment.getName() + ": " + status.getMessage());
      }
      waitForCompletion(status);

      // Start the modules whose IDs are returned by the "distribute" operation.:
      TargetModuleID[] moduleIDs = progress.getResultTargetModuleIDs();
      progress = deploymentManager.start(moduleIDs);
      status = progress.getDeploymentStatus();
      waitForCompletion(status);
      
      return progress.getResultTargetModuleIDs();
   }
   
   @Override
   public void undeploy(final Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      
      try
      {
         undeploy();
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not undeploy " + archive.getName(), e);
      }
   }
   
   @Override
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      try
      {
         undeploy();
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not undeploy " + descriptor.getDescriptorName(), e);
      }
   }

   private void undeploy() throws DeploymentException
   {
      if (targetModuleIDs.length == 0)
         return;
      
      ProgressObject stopProgress = deploymentManager.stop(targetModuleIDs);
      DeploymentStatus stopStatus = stopProgress.getDeploymentStatus();
      waitForCompletion(stopStatus);
      if (isNotCompleted(stopStatus))
      {
         throw new DeploymentException("Stop deployment not completed: " + stopStatus.getMessage());
      }

      ProgressObject undeployProgress = deploymentManager.undeploy(targetModuleIDs);
      DeploymentStatus undeployStatus = undeployProgress.getDeploymentStatus();
      waitForCompletion(undeployStatus);
      if (isNotCompleted(undeployStatus))
      {
         throw new DeploymentException("Undeploy not completed: " + undeployStatus.getMessage());
      }

      targetModuleIDs = EMPTY_ARRAY;
   }

   @Override
   public void stop() throws LifecycleException
   {
   }

   private boolean isNotCompleted(DeploymentStatus status)
   {
      return StateType.COMPLETED != status.getState();
   }

   /**
    * Wait for completion of a DeploymentStatus (wait as long as the "StateType" is "RUNNING")
    * 
    * @param status This is the DeploymentStatus on whose completion we wait.
    * @throws DeploymentException 
    */
   private static void waitForCompletion(DeploymentStatus status) throws DeploymentException
   {
      try
      {
         while (StateType.RUNNING == status.getState())
            Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
         throw new DeploymentException("Failed to deploy: " + e.getMessage());
      }
   }
   
   private MBeanServerConnection getServerConnection() throws Exception
   {
      String adapterName = "jmx/rmi/RMIAdaptor";
      if (serverConnection == null)
      {
         Object obj = createContext().lookup(adapterName);
         if (obj == null)
         {
            throw new NameNotFoundException("Object " + adapterName + " not found.");
         }

         serverConnection = ((MBeanServerConnection) obj);
      }
      return serverConnection;
   }
}