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
package org.jboss.arquillian.jbossas.local60;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.arquillian.jbossas.local60.utils.AsLifecycleDelegate;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
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

   private static AsLifecycleDelegate delegate;

   protected Server server;

   protected ServerManager manager;

   private boolean wasStarted;

   private final List<String> failedUndeployments = new ArrayList<String>();

   private Boolean forceRestart = false;

   private Integer shutdownDelay = 15000;

   private Long bootTimeout = 240000L;

   private String host = "localhost";

   private int port = 8181;
   
   private String profile = "default";

   private JBossASConfiguration configuration;

   public JBossASLocalContainer()
   {

   }

   /* (non-Javadoc)
   * @see org.jboss.arquillian.spi.DeployableContainer#setup(org.jboss.arquillian.spi.Context, org.jboss.arquillian.spi.Configuration)
   */
   public void setup(Context context, Configuration configuration)
   {
      this.configuration = configuration.getContainerConfig(JBossASConfiguration.class);
      host = this.configuration.getBindAddress();
      profile = this.configuration.getProfileName();
      port = this.configuration.getHttpPort();
   }

   /* (non-Javadoc)
   * @see org.jboss.arquillian.spi.DeployableContainer#start(org.jboss.arquillian.spi.Context)
   */
   public void start(Context context) throws LifecycleException
   {
      try
      {
         startServerManager();
         restartServer();
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
      try
      {
         removeFailedUnDeployments();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not clean up", e);
      }
      if (wasStarted)
      {
         stopServer();
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
      if (manager == null || server == null)
      {
         throw new IllegalStateException("start has not been called!");
      }
      final String deploymentName = archive.getName();

      File file = new File(deploymentName);
      archive.as(ZipExporter.class).exportZip(file, true);

      Exception failure = null;
      try
      {
         server.deploy(file);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not deploy " + deploymentName, e);
      }
      if (failure != null)
      {
         throw new DeploymentException("Failed to deploy " + deploymentName, failure);
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
      File file = new File(archive.getName());
      archive.as(ZipExporter.class).exportZip(file, true);
      undeploy(file);
   }

   private void undeploy(File file) throws DeploymentException
   {
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

   protected void startServerManager()
   {
      manager = getDelegate().getServerManager();
      server = new Server();
      server.setName(profile);
      server.setHttpPort(port);
      server.setHost(host);
      
      if(configuration.getJbossHome() != null) 
      {
         manager.setJbossHome(configuration.getJbossHome());
      }
      if(configuration.getJavaHome() != null) 
      {
         manager.setJavaHome(configuration.getJavaHome());
      }

      AsLifecycleDelegate.applyServerDefaults(server, manager);
   }

   protected void restartServer() throws IOException, LifecycleException
   {
      if (getForceRestart())
      {
         if (isServerUp())
         {
            log.info("Shutting down server as in force-restart mode");
            stopServer();
            try
            {
               Thread.sleep(getShutdownDelay());
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
         }
      }
      if (!isServerUp())
      {
         wasStarted = true;
         startServer();
         log.info("Starting server");
         // Wait for server to come up
         long timeoutTime = System.currentTimeMillis() + getServerBootTimeout();
         boolean interrupted = false;
         while (timeoutTime > System.currentTimeMillis())
         {
            if (isServerUp())
            {
               log.info("Started server");
               return;
            }
            try
            {
               Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
               interrupted = true;
            }
         }
         if (interrupted)
         {
            Thread.currentThread().interrupt();
         }
         // If we got this far something went wrong
         log.info("Unable to connect to server after " + getServerBootTimeout() + "ms, giving up!");
         stopServer();
         throw new IllegalStateException("Error connecting to server");
      }
   }

   protected void stopServer() throws LifecycleException
   {
      try
      {
         getDelegate().stopJbossAs(profile);
      }
      catch (Throwable t)
      {
         throw new LifecycleException("could not stop local container", t);
      }
   }

   private void startServer() throws LifecycleException
   {
      try
      {
         getDelegate().startJbossAs(profile);
      }
      catch (Throwable t)
      {
         throw new LifecycleException("could not start local container", t);
      }
   }

   protected boolean isServerUp() throws IOException
   {
      return ServerController.isServerStarted(server);
   }

   protected synchronized static AsLifecycleDelegate getDelegate()
   {
      if (delegate == null)
      {
         delegate = new AsLifecycleDelegate();
      }
      return delegate;
   }

   protected String getHost()
   {
      return host;
   }

   protected Boolean getForceRestart()
   {
      return forceRestart;
   }

   protected Integer getShutdownDelay()
   {
      return shutdownDelay;
   }

   protected Long getServerBootTimeout()
   {
      return bootTimeout;
   }
   
   public int getPort()
   {
      return port;
   }
}
