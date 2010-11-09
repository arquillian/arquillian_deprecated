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
package org.jboss.arquillian.container.jbossas.remote_6;

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
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * JbossRemoteContainer
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JBossASRemoteContainer implements DeployableContainer<JBossASConfiguration>
{
   private final List<String> failedUndeployments = new ArrayList<String>();
   private DeploymentManager deploymentManager;

   private HttpServer httpFileServer;
   
   private JBossASConfiguration configuration;
   
   private InitialContext context;
   
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 3.0");
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
         // TODO: configure http bind address
         httpFileServer = HttpServer.create();
         httpFileServer.bind(
               new InetSocketAddress(
                     InetAddress.getByName(configuration.getLocalDeploymentBindAddress()), 
                     configuration.getLocalDeploymentBindPort()), 
               -1);
         httpFileServer.start();
         initDeploymentManager();
         stopDeploymentScanner();
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
         httpFileServer.stop(0);
         removeFailedUnDeployments();
         startDeploymentScanner();
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not clean up", e);
      }
   }

   public ProtocolMetaData deploy(final Deployment... deployments) throws DeploymentException
   {
      if(deployments == null) 
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      if (deploymentManager == null)
      {
         throw new IllegalStateException("start has not been called!");
      }
      for(final Deployment deployment : deployments)
      {
         String deploymentName = deployment.getName();

         Exception failure = null;
         try
         {
            httpFileServer.createContext("/" + deploymentName, new HttpHandler()
            {
               public void handle(HttpExchange exchange) throws IOException
               {
                  if(deployment.isArchiveDeployment())
                  {
                     InputStream zip = deployment.getArchive().as(ZipExporter.class).exportAsInputStream();
                     ByteArrayOutputStream zipStream = new ByteArrayOutputStream();
                     JBossASRemoteContainer.copy(zip, zipStream);
                     zip.close();

                     byte[] zipArray = zipStream.toByteArray();
                     exchange.sendResponseHeaders(200, zipArray.length);

                     OutputStream out = exchange.getResponseBody();
                     out.write(zipArray);
                     out.close();                     

                  }
                  else
                  {
                     exchange.sendResponseHeaders(200, -1);

                     OutputStream out = exchange.getResponseBody();
                     out.write(deployment.getDescriptor().exportAsString().getBytes());
                     out.close();
                  }
               }
            });
            URL fileServerUrl = createFileServerURL(deploymentName);
            
            DeploymentProgress distribute = deploymentManager.distribute(deploymentName, fileServerUrl, true);
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
      return new ProtocolMetaData()
               .addContext(new HTTPContext(
                     configuration.getRemoteServerAddress(), 
                     configuration.getRemoteServerHttpPort(), 
                     "test"));
   }

   public void undeploy(final Deployment... deployments) throws DeploymentException
   {
      if(deployments == null) 
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      for(Deployment deployment : deployments)
      {
         undeploy(deployment.getName());
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
      InitialContext ctx = createContext();
      ProfileService ps = (ProfileService) ctx.lookup("ProfileService");

      deploymentManager = ps.getDeploymentManager();
      ProfileKey defaultKey = new ProfileKey(profileName);
      deploymentManager.loadProfile(defaultKey);
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
            DeploymentProgress undeployProgress = deploymentManager.remove(name);
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

   /*
    * JBoss AS 6.0 M4 has problems when using the ProfileService for deployment. Both the DeploymentManager and the HDScanner
    * tried to deploy the same file. Since the HDScanner is lagging behind, it will undeploy what the DeploymentManager deployed and redeploy.
    * 
    *  Stop the HDScanner before we start to deploy, then start it again when we're done. 
    */
   private void startDeploymentScanner() throws Exception
   {
      ObjectName DEPLOYMNET_SCANNER = new ObjectName("jboss.deployment:flavor=URL,type=DeploymentScanner");
      MBeanServerConnection adaptor = (MBeanServerConnection)createContext().lookup("jmx/invoker/RMIAdaptor");
      adaptor.invoke(DEPLOYMNET_SCANNER, "start", new Object[] {}, new String[]{});
   }

   private void stopDeploymentScanner() throws Exception
   {
      ObjectName DEPLOYMNET_SCANNER = new ObjectName("jboss.deployment:flavor=URL,type=DeploymentScanner");
      MBeanServerConnection adaptor = (MBeanServerConnection)createContext().lookup("jmx/invoker/RMIAdaptor");
      adaptor.invoke(DEPLOYMNET_SCANNER, "stop", new Object[] {}, new String[]{});
   }
}
