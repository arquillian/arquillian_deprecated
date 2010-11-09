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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;

import org.jboss.arquillian.protocol.servlet_2_5.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.managed.api.ManagedDeployment.DeploymentPhase;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.virtual.VFS;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * JbossRemoteContainer
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JBossASRemoteContainer implements DeployableContainer
{
   private final List<String> failedUndeployments = new ArrayList<String>();
   private DeploymentManager deploymentManager;

   private HttpServer httpFileServer;
   
   private JBossASConfiguration configuration;
   
   public void setup(Context context, Configuration configuration)
   {
      this.configuration = configuration.getContainerConfig(JBossASConfiguration.class);
   }
   
   public void start(Context context) throws LifecycleException
   {
      try 
      {
         // TODO: configure http bind address
         httpFileServer = HttpServer.create();
         httpFileServer.bind(
               new InetSocketAddress(
                     InetAddress.getByName(configuration.getLocalDeploymentBindAddress()), 
                     configuration.getLocalDeploymentBindPort()), 
               -1);
         httpFileServer.start();
         initDeploymentManager();
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not connect to container", e);
      }
   }
   
   public void stop(Context context) throws LifecycleException
   {
      try 
      {
         httpFileServer.stop(0);
         removeFailedUnDeployments();
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not clean up", e);
      }
   }

   public ContainerMethodExecutor deploy(Context context, final Archive<?> archive) throws DeploymentException
   {
      if(archive == null) 
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      if (deploymentManager == null)
      {
         throw new IllegalStateException("start has not been called!");
      }
      String deploymentName = archive.getName();
      
      Exception failure = null;
      try
      {
         httpFileServer.createContext("/" + deploymentName, new HttpHandler()
         {
            public void handle(HttpExchange exchange) throws IOException
            {
               InputStream zip = archive.as(ZipExporter.class).exportZip();
               ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
               JBossASRemoteContainer.copy(zip, zipStream);
               zip.close();

               byte[] zipArray = zipStream.toByteArray();
               exchange.sendResponseHeaders(200, zipArray.length);

               OutputStream out = exchange.getResponseBody();
               out.write(zipArray);
               out.close();

            }
         });
         URL fileServerUrl = createFileServerURL(deploymentName);
         
         DeploymentProgress distribute = deploymentManager.distribute(deploymentName, DeploymentPhase.APPLICATION, fileServerUrl, true);
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
      try 
      {
         return new ServletMethodExecutor(
               new URL(
                     "http",
                     configuration.getRemoteServerAddress(),
                     configuration.getRemoteServerHttpPort(), 
                     "/")
               );
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not create ContianerMethodExecutor", e);
      }
   }

   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      if(archive == null) 
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      undeploy(archive.getName());
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
         httpFileServer.removeContext("/" + name);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not undeploy " + name, e);
      }
   }

   private void initDeploymentManager() throws Exception 
   {
      String profileName = configuration.getProfileName();
      InitialContext ctx = new InitialContext();
      ProfileService ps = (ProfileService) ctx.lookup("ProfileService");
      deploymentManager = ps.getDeploymentManager();
      ProfileKey defaultKey = new ProfileKey(profileName);
      deploymentManager.loadProfile(defaultKey, false);
      VFS.init();
   }
   
   private URL createFileServerURL(String archiveName) 
   {
      try 
      {
         InetSocketAddress address = httpFileServer.getAddress();
         return new URL(
               "http", 
               address.getHostName(), 
               address.getPort(), 
               "/" + archiveName);
      }
      catch (MalformedURLException e) 
      {
         throw new RuntimeException("Could not create fileserver url", e);
      }
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

   private static void copy(InputStream source, OutputStream destination) throws IOException
   {
      if (source == null)
      {
         throw new IllegalArgumentException("source must be specified");
      }
      if (destination == null)
      {
         throw new IllegalArgumentException("destination must be specified");
      }
      byte[] readBuffer = new byte[2156]; 
      int bytesIn = 0; 
      while((bytesIn = source.read(readBuffer)) != -1) 
      { 
         destination.write(readBuffer, 0, bytesIn); 
      }
   }
}
