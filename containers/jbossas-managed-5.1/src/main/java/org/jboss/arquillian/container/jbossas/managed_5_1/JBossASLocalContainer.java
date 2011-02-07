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
package org.jboss.arquillian.container.jbossas.managed_5_1;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.Context;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.jbossas.servermanager.Argument;
import org.jboss.jbossas.servermanager.Property;
import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerController;
import org.jboss.jbossas.servermanager.ServerManager;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * JbossLocalContainer
 *
 * @author <a href="mailto:aamonten@gmail.com">Alejandro Montenegro</a>
 * @version $Revision: $
 */
public class JBossASLocalContainer implements DeployableContainer<JBossASConfiguration>
{
   private static Logger log = Logger.getLogger(JBossASLocalContainer.class.getName());

   private JBossASConfiguration configuration;

   protected ServerManager manager;

   private final List<String> failedUndeployments = new ArrayList<String>();

   private ProfileService profileService;
   private DeploymentManager deploymentManager;

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
      
      manager = createAndConfigureServerManager();
   }
   
   public void start() throws LifecycleException
   {
      try
      {
         Server server = manager.getServer(configuration.getProfileName());
         if(ServerController.isServerStarted(server))
         {
            throw new LifecycleException(
                  "The server is already running! " +
                  "Managed containers does not support connecting to running server instances due to the " +
                  "possible harmfull effect of connecting to the wrong server. Please stop server before running or " +
                  "change to another type of container.");
         }
         
         manager.startServer(server.getName());
         initProfileService(server);
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not start remote container", e);
      }
   }

   public void stop() throws LifecycleException
   {
      Server server = manager.getServer(configuration.getProfileName());
      if(!server.isRunning())
      {
         throw new LifecycleException("Can not stop server. Server is not started");
      }
      try
      {
         removeFailedUnDeployments();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not clean up failed undeployments", e);
      }
      
      try
      {
         manager.stopServer(server.getName());
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not stop server", e);
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
      final String deploymentName = archive.getName();
      URL deploymentUrl = ShrinkWrapUtil.toURL(archive);

      deploy(deploymentName, deploymentUrl);
      try
      {
         return ManagementViewParser.parse(deploymentName, profileService);
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

   private void removeFailedUnDeployments() throws IOException
   {
      Server server = manager.getServer(configuration.getProfileName());
      
      List<String> remainingDeployments = new ArrayList<String>();
      for (String name : failedUndeployments)
      {
         try
         {
            server.undeploy(new File(name));
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
         log.severe("Failed to undeploy these artifacts: " + remainingDeployments);
      }
      failedUndeployments.clear();
   }

   private void initProfileService(Server server) throws Exception 
   {
      String profileName = configuration.getProfileName();
      Context ctx = server.getNamingContext();
      profileService = (ProfileService) ctx.lookup("ProfileService");

      deploymentManager = profileService.getDeploymentManager();

      ProfileKey defaultKey = new ProfileKey(profileName);
      deploymentManager.loadProfile(defaultKey);
   }

   /*
    * Internal Helpers for Creating and Configuring ServerManager and Server.
    */
   
   private ServerManager createAndConfigureServerManager()
   {
      ServerManager manager = new ServerManager();
      if(configuration.getJbossHome() != null) 
      {
         manager.setJbossHome(configuration.getJbossHome());
      }
      if(configuration.getJavaHome() != null) 
      {
         manager.setJavaHome(configuration.getJavaHome());
      }
      manager.addServer(createAndConfigureServer());
      return manager;
   }

   private Server createAndConfigureServer()
   {
      Server server = new Server();
      server.setName(configuration.getProfileName());
      server.setHttpPort(configuration.getHttpPort());
      server.setHost(configuration.getBindAddress());
      
      server.setUsername("admin");
      server.setPassword("admin");
      server.setPartition(Long.toHexString(System.currentTimeMillis()));

      // Set server's JVM arguments
      setServerVMArgs(server, configuration.getJavaVmArguments());

      // Set server's system properties
      Property prop = new Property();
      prop.setKey("jbosstest.udp.ip_ttl");
      prop.setValue("0");
      server.addSysProperty(prop);
      prop = new Property();
      prop.setKey("java.endorsed.dirs");
      prop.setValue(new File(configuration.getJbossHome(), "lib/endorsed").getAbsolutePath());
      server.addSysProperty(prop);
      
      return server;
   }

   private void setServerVMArgs(Server server, String arguments)
   {
      for(String argument: arguments.split(" "))
      {
         Argument arg = new Argument();
         arg.setValue(argument);
         server.addJvmArg(arg);
      }
   }
}
