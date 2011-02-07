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
package org.jboss.arquillian.container.jbossas.embedded_6;

import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.embedded.api.server.JBossASEmbeddedServer;
import org.jboss.embedded.api.server.JBossASEmbeddedServerFactory;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * JBoss Embedded 6.0 Container implementation. <br/>
 * 
 * Embedded uses MainDeployer which seems to not register the correct information with the ProfileService so we can retrieve 
 * it for inspection of Servlet etc. So we do not use embedded.deploy but rather lookup the ProfileService and deploy via that.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JBossASEmbeddedContainer implements DeployableContainer<JBossASContainerConfiguration>
{
   private JBossASEmbeddedServer server;
   private ProfileService profileService;
   private DeploymentManager deploymentManager;
   private InitialContext context;
   
   private JBossASContainerConfiguration configuration;
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getDefaultProtocol()
    */
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 3.0");
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getConfigurationClass()
    */
   public Class<JBossASContainerConfiguration> getConfigurationClass()
   {
      return JBossASContainerConfiguration.class;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#setup(org.jboss.arquillian.spi.client.container.ContainerConfiguration)
    */
   public void setup(JBossASContainerConfiguration configuration)
   {
      this.configuration = configuration;

      server = JBossASEmbeddedServerFactory.createServer();
      server.getConfiguration()
               .bindAddress(configuration.getBindAddress())
               .serverName(configuration.getProfileName());
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#start()
    */
   public void start() throws LifecycleException
   {
      try 
      {
         server.start();
         initDeploymentManager();
      }
      catch (Exception e) 
      {
         throw new LifecycleException("Could not start container", e);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#stop()
    */
   public void stop() throws LifecycleException
   {
      try 
      {
         server.stop();
      }
      catch (Exception e) 
      {
         throw new LifecycleException("Could not stop container", e);
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

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.arquillian.spi.client.deployment.Deployment[])
    */
   public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException
   {
      String deploymentName = archive.getName();
      URL deploymentUrl = ShrinkWrapUtil.toURL(archive);
      
      deploy(deploymentName, deploymentUrl);
      try
      {
         return ManagementViewParser.parse(archive.getName(), (ProfileService)new InitialContext().lookup("ProfileService"));
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not extract deployment metadata", e);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.arquillian.spi.client.deployment.Deployment[])
    */
   public void undeploy(final Archive<?> archive) throws DeploymentException
   {
      try 
      {
         undeploy(archive.getName());
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not undeploy from container", e);
      }
   }


   private void initDeploymentManager() throws Exception 
   {
      String profileName = configuration.getProfileName();
      Context ctx = createContext();
      profileService = (ProfileService) ctx.lookup("ProfileService");
      
      deploymentManager = profileService.getDeploymentManager();

      ProfileKey defaultKey = new ProfileKey(profileName);
      deploymentManager.loadProfile(defaultKey);
   }
   
   private InitialContext createContext() throws Exception
   {
      if(context == null)
      {
         context = new InitialContext();
      }
      return context;
   }
   
   private void deploy(String deploymentName, URL url) throws DeploymentException
   {
      Exception failure = null;
      try
      {
         DeploymentProgress distribute = deploymentManager.distribute(deploymentName, url, true);
         distribute.run();
         DeploymentStatus uploadStatus = distribute.getDeploymentStatus(); 
         if(uploadStatus.isFailed()) 
         {
            failure = uploadStatus.getFailure();
            undeploy(deploymentName);
         } 
         else 
         {
            DeploymentProgress progress = deploymentManager.start(deploymentName);
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
         DeploymentProgress stopProgress = deploymentManager.stop(name);
         stopProgress.run();

         DeploymentProgress undeployProgress = deploymentManager.remove(name);
         undeployProgress.run();
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not undeploy " + name, e);
      }
   }
}