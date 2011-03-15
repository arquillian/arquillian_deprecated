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
package org.jboss.arquillian.container.jbossas.remote_5_0;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.managed.api.ManagedDeployment.DeploymentPhase;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.virtual.VFS;

/**
 * JbossRemoteContainer
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JBossASRemoteContainer implements DeployableContainer<JBossASConfiguration>
{
   private final List<String> failedUndeployments = new ArrayList<String>();
   private ProfileService profileService;
   private DeploymentManager deploymentManager;
   private InitialContext context;
   private MBeanServerConnection serverConnection;

   private JBossASConfiguration configuration;

   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 2.5");
   }
   
   public Class<JBossASConfiguration> getConfigurationClass()
   {
      return JBossASConfiguration.class;
   }
   
   public void setup(JBossASConfiguration configuration)
   {
      this.configuration = configuration;
   }
   
   public void start() throws LifecycleException
   {
      try 
      {
         initDeploymentManager();
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
         removeFailedUnDeployments();
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not clean up", e);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      // TODO Auto-generated method stub
      
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      // TODO Auto-generated method stub
      
   }

   public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException
   {
      String deploymentName = archive.getName();
      URL deploymentUrl = ShrinkWrapUtil.toURL(archive);

      deploy(deploymentName, deploymentUrl);
      
      try
      {
         return ManagementViewParser.parse(deploymentName, getServerConnection());
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not extract deployment metadata", e);
      }
   }

   public void undeploy(final Archive<?> archive) throws DeploymentException
   {
      undeploy(archive.getName());
   }

   private void deploy(String deploymentName, URL url) throws DeploymentException
   {
      Exception failure = null;
      try
      {
         DeploymentProgress distribute = deploymentManager.distribute(deploymentName, DeploymentPhase.APPLICATION, url, true);
         distribute.run();
         DeploymentStatus uploadStatus = distribute.getDeploymentStatus(); 
         if(uploadStatus.isFailed()) 
         {
            failure = uploadStatus.getFailure();
            undeploy(deploymentName);
         } 
         else 
         {
            DeploymentProgress progress = deploymentManager.start(DeploymentPhase.APPLICATION, deploymentName);
            progress.run();
            DeploymentStatus status = progress.getDeploymentStatus();
            if (status.isFailed())
            {
               failure = status.getFailure();
               undeploy(deploymentName);
            }
         }
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not deploy " + deploymentName, e);
      }
      if (failure != null)
      {
         throw new DeploymentException("Failed to deploy " + deploymentName, failure);
      }
   }

   private void undeploy(String name) throws DeploymentException
   {
      try
      {
         DeploymentProgress stopProgress = deploymentManager.stop(DeploymentPhase.APPLICATION, name);
         stopProgress.run();

         DeploymentProgress undeployProgress = deploymentManager.undeploy(DeploymentPhase.APPLICATION, name);
         undeployProgress.run();
         if (undeployProgress.getDeploymentStatus().isFailed())
         {
            failedUndeployments.add(name);
         }
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not undeploy " + name, e);
      }
   }

   private void initDeploymentManager() throws Exception 
   {
      String profileName = configuration.getProfileName();
      InitialContext ctx = createContext();
      profileService = (ProfileService) ctx.lookup("ProfileService");
      deploymentManager = profileService.getDeploymentManager();
      ProfileKey defaultKey = new ProfileKey(profileName);
      deploymentManager.loadProfile(defaultKey, false);
      VFS.init();
   }
   
   private InitialContext createContext() throws Exception
   {
      if(context == null)
      {
         Properties props = new Properties();
         props.put(InitialContext.INITIAL_CONTEXT_FACTORY, configuration.getContextFactory());
         props.put(InitialContext.URL_PKG_PREFIXES, configuration.getUrlPkgPrefix());
         props.put(InitialContext.PROVIDER_URL, configuration.getProviderUrl());
         context = new InitialContext(props);
      }
      return context;
   }
   
   public MBeanServerConnection getServerConnection() throws Exception
   {
      String adapterName = "jmx/rmi/RMIAdaptor";
      if ( serverConnection == null)
      {
         Object obj = createContext().lookup(adapterName);
         if ( obj == null )
         {
            throw new NameNotFoundException("Object " + adapterName + " not found.");
         }

         serverConnection = ((MBeanServerConnection) obj);
      }
      return serverConnection;
   }

   private void removeFailedUnDeployments() throws IOException
   {
      List<String> remainingDeployments = new ArrayList<String>();
      for (String name : failedUndeployments)
      {
         try
         {
            DeploymentProgress undeployProgress = deploymentManager.undeploy(DeploymentPhase.APPLICATION, name);
            undeployProgress.run();
            if (undeployProgress.getDeploymentStatus().isFailed())
            {
               remainingDeployments.add(name);
            }
         }
         catch (Exception e)
         {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
         }
      }
      if (remainingDeployments.size() > 0)
      {
         //log.error("Failed to undeploy these artifacts: " + remainingDeployments);
      }
      failedUndeployments.clear();
   }
}
