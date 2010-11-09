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

import org.jboss.arquillian.protocol.servlet_2_5.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.jbossas.servermanager.Argument;
import org.jboss.jbossas.servermanager.Property;
import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerController;
import org.jboss.jbossas.servermanager.ServerManager;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * JbossLocalContainer
 *
 * @author <a href="mailto:aamonten@gmail.com">Alejandro Montenegro</a>
 * @version $Revision: $
 */
public class JBossASLocalContainer implements DeployableContainer
{
   private static Logger log = Logger.getLogger(JBossASLocalContainer.class.getName());

   private JBossASConfiguration configuration;

   protected ServerManager manager;

   private final List<String> failedUndeployments = new ArrayList<String>();

   /* (non-Javadoc)
   * @see org.jboss.arquillian.spi.DeployableContainer#setup(org.jboss.arquillian.spi.Context, org.jboss.arquillian.spi.Configuration)
   */
   public void setup(Context context, Configuration configuration)
   {
      this.configuration = configuration.getContainerConfig(JBossASConfiguration.class);
      
      manager = createAndConfigureServerManager();
   }
   
   /* (non-Javadoc)
   * @see org.jboss.arquillian.spi.DeployableContainer#start(org.jboss.arquillian.spi.Context)
   */
   public void start(Context context) throws LifecycleException
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
      }
      catch (IOException e)
      {
         throw new LifecycleException("Could not start remote container", e);
      }
   }

   /* (non-Javadoc)
   * @see org.jboss.arquillian.spi.DeployableContainer#stop(org.jboss.arquillian.spi.Context)
   */
   public void stop(Context context) throws LifecycleException
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
    * @see org.jboss.arquillian.spi.DeployableContainer#deploy(org.jboss.arquillian.spi.Context, org.jboss.shrinkwrap.api.Archive)
    */
   public ContainerMethodExecutor deploy(Context context, final Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      if (manager == null)
      {
         throw new IllegalStateException("Container has not been setup");
      }
      final String deploymentName = archive.getName();

      File file = new File(deploymentName);
      archive.as(ZipExporter.class).exportZip(file, true);

      Server server = manager.getServer(configuration.getProfileName());
      try
      {
         server.deploy(file);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not deploy " + deploymentName, e);
      }
      try
      {
         return new ServletMethodExecutor(new URL(server.getHttpUrl().toExternalForm() + "/"));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create ContainerMethodExecutor", e);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#undeploy(org.jboss.arquillian.spi.Context, org.jboss.shrinkwrap.api.Archive)
    */
   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      // we only need the File, not the content to undeploy.
      File file = new File(archive.getName());
      undeploy(file);
   }

   private void undeploy(File file) throws DeploymentException
   {
      Server server = manager.getServer(configuration.getProfileName());
      try
      {
         server.undeploy(file);
      }
      catch (Exception e)
      {
         failedUndeployments.add(file.getName());
         throw new DeploymentException("Could not undeploy " + file.getName(), e);
      } 
      finally
      {
         file.delete();
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
