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
package org.jboss.arquillian.container.jbossas.managed_4_2;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.jbossas.servermanager.Argument;
import org.jboss.jbossas.servermanager.Property;
import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerController;
import org.jboss.jbossas.servermanager.ServerManager;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * JBossASLocalContainer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author Davide D'Alto
 * @version $Revision: $
 */
public class JBossASLocalContainer implements DeployableContainer<JBossASConfiguration>
{

   private static final TargetModuleID[] EMPTY_ARRAY = new TargetModuleID[0];

   private JBossASConfiguration configuration;

   private DeploymentManager deploymentManager;
   
   protected ServerManager serverManager;
   
   private TargetModuleID[] targetModuleIDs = EMPTY_ARRAY;
   
   @Override
   public void setup(JBossASConfiguration configuration)
   {
      this.configuration = configuration;
      this.serverManager = createAndConfigureServerManager();
   }

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
   public void start() throws LifecycleException
   {
      try
      {
         Server server = serverManager.getServer(configuration.getProfileName());
         if (ServerController.isServerStarted(server))
         {
            throw new LifecycleException(
                  "The server is already running! " +
                  "Managed containers does not support connecting to running server instances due to the " +
                  "possible harmfull effect of connecting to the wrong server. Please stop server before running or " +
                  "change to another type of container.");
         }

         serverManager.startServer(server.getName());
         deploymentManager = createDeploymentManager(server);
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not connect to container", e);
      }
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
         Server server = serverManager.getServer(configuration.getProfileName());
         return ManagementViewParser.parse(archive.getName(), server.getServerConnection());
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
         throw new DeploymentException("Stop deployment not completed: " + stopStatus.getMessage());

      ProgressObject undeployProgress = deploymentManager.undeploy(targetModuleIDs);
      DeploymentStatus undeployStatus = undeployProgress.getDeploymentStatus();
      waitForCompletion(undeployStatus);
      if (isNotCompleted(undeployStatus))
         throw new DeploymentException("Undeploy not completed: " + undeployStatus.getMessage());

      targetModuleIDs = EMPTY_ARRAY;
   }

   @Override
   public void stop() throws LifecycleException
   {
      Server server = serverManager.getServer(configuration.getProfileName());
      if (!server.isRunning())
      {
         throw new LifecycleException("Can not stop server. Server is not started");
      }

      try
      {
         serverManager.stopServer(server.getName());
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not stop server", e);
      }
   }
   
   private DeploymentManager createDeploymentManager(Server server) throws DeploymentManagerCreationException
   {
      DeploymentFactoryManager dfm = DeploymentFactoryManager.getInstance();
      return dfm.getDeploymentManager(server.getServerUrl(), null, null);
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
   
   /*
    * Internal Helpers for Creating and Configuring ServerManager and Server.
    */
   
   private ServerManager createAndConfigureServerManager()
   {
      ServerManager manager = new ArquillianServerManager(
            configuration.getStartupTimeoutInSeconds(),
            configuration.getShutdownTimeoutInSeconds());

      if (configuration.getJbossHome() != null)
      {
         manager.setJbossHome(configuration.getJbossHome());
      }
      if (configuration.getJavaHome() != null)
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
      server.setRmiPort(configuration.getRmiPort());
      server.setHost(configuration.getBindAddress());
      server.setHasWebServer(!configuration.isUseRmiPortForAliveCheck());

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
      for (String argument : arguments.split(" "))
      {
         Argument arg = new Argument();
         arg.setValue(argument);
         server.addJvmArg(arg);
      }
   }

}