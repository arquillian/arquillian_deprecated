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
package org.jboss.arquillian.container.tomcat.embedded_6;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Embedded;
import org.jboss.arquillian.protocol.servlet_3.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.tomcat.api.ShrinkWrapStandardContext;

/**
 * Arquillian {@link DeployableContainer} adaptor for a target Tomcat
 * environment; responsible for lifecycle and deployment operations
 * 
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @author Dan Allen
 * @version $Revision: $
 */
public class TomcatContainer implements DeployableContainer
{
   private static final String ENV_VAR = "${env.";

   private static final String HTTP_PROTOCOL = "http";

   private static final String SEPARATOR = "/";

   private static final Logger log = Logger.getLogger(TomcatContainer.class.getName());

   /**
    * Tomcat embedded
    */
   private Embedded embedded;

   /**
    * Engine contained within Tomcat embedded
    */
   private Engine engine;

   /**
    * Host contained in the tomcat engine
    */
   private Host standardHost;

   /**
    * Tomcat container configuration
    */
   private TomcatConfiguration configuration;

   private String serverName;

   private String bindAddress;

   private int bindPort;

   private boolean wasStarted;

   private final List<String> failedUndeployments = new ArrayList<String>();

   public void setup(Context context, Configuration configuration)
   {
      this.configuration = configuration.getContainerConfig(TomcatConfiguration.class);
      bindAddress = this.configuration.getBindAddress();
      bindPort = this.configuration.getBindHttpPort();
      serverName = this.configuration.getServerName();
   }

   public void start(Context context) throws LifecycleException
   {
      try
      {
         startTomcatEmbedded();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Bad shit happened", e);
      }
   }

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
         try
         {
            stopTomcatEmbedded();
         }
         catch (org.apache.catalina.LifecycleException e)
         {
            throw new LifecycleException("An unexpected error occurred", e);
         }
      }
   }

   public ContainerMethodExecutor deploy(Context context, final Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      if (embedded == null)
      {
         throw new IllegalStateException("start has not been called!");
      }

      try
      {
         StandardContext standardContext = archive.as(ShrinkWrapStandardContext.class);
         standardContext.setParent(standardHost);
         if (configuration.getTomcatWorkDir() != null)
         {
            standardContext.setWorkDir(configuration.getTomcatWorkDir());
         }
         standardContext.setUnpackWAR(configuration.isUnpackArchive());
         standardHost.addChild(standardContext);
         context.add(StandardContext.class, standardContext);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Failed to deploy " + archive.getName(), e);
      }

      try
      {
         return new ServletMethodExecutor(
            new URL(
               HTTP_PROTOCOL,
               bindAddress,
               bindPort,
               "/"));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create ContainerMethodExecutor", e);
      }
   }

   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      StandardContext standardContext = context.get(StandardContext.class);
      if (standardContext != null)
      {
         standardHost.removeChild(standardContext);
      }
   }

   private void undeploy(String name) throws DeploymentException
   {
      Container child = standardHost.findChild(name);
      if (child != null)
      {
         standardHost.removeChild(child);
      }
   }

   private void removeFailedUnDeployments() throws IOException
   {
      List<String> remainingDeployments = new ArrayList<String>();
      for (String name : failedUndeployments)
      {

         try
         {
            undeploy(name);

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

   protected void startTomcatEmbedded() throws UnknownHostException, org.apache.catalina.LifecycleException
   {
      // creating the tomcat embedded == service tag in server.xml
      embedded = new Embedded();
      embedded.setName(serverName);
      // TODO this needs to be a lot more robust
      String tomcatHome = configuration.getTomcatHome();
      File tomcatHomeFile = null;
      if (tomcatHome != null)
      {
         if (tomcatHome.startsWith(ENV_VAR))
         {
            String sysVar = tomcatHome.substring(ENV_VAR.length(), tomcatHome.length() - 1);
            tomcatHome = System.getProperty(sysVar);
            tomcatHomeFile = new File(tomcatHome);
            log.info("Using tomcat home from environment variable: " + tomcatHome);
         }
         if (tomcatHome != null)
         {
            tomcatHomeFile = new File(tomcatHome);
            tomcatHome = tomcatHomeFile.getAbsolutePath();
            embedded.setCatalinaBase(tomcatHome);
            embedded.setCatalinaHome(tomcatHome);
         }
         if (tomcatHomeFile != null)
         {
            tomcatHomeFile.mkdirs();
         }
      }
      // creates the engine == engine tag in server.xml
      engine = embedded.createEngine();
      engine.setName(serverName);
      engine.setDefaultHost(bindAddress);
      engine.setService(embedded);
      embedded.setContainer(engine);
      embedded.addEngine(engine);
      // creates the host == host tag in server.xml
      if (tomcatHomeFile != null)
      {
         File appBaseFile = new File(tomcatHomeFile, configuration.getAppBase());
         appBaseFile.mkdirs();
         standardHost = embedded.createHost(bindAddress + SEPARATOR, appBaseFile.getAbsolutePath());
      }
      else
      {
         standardHost = embedded.createHost(bindAddress + SEPARATOR, System.getProperty("java.io.tmpdir"));
      }
      standardHost.setParent(engine);
      engine.addChild(standardHost);
      // creates an http connector == connector in server.xml
      // TODO externalize this stuff in the configuration
      Connector connector = embedded.createConnector(InetAddress.getByName(bindAddress), bindPort, false);
      embedded.addConnector(connector);
      connector.setContainer(engine);
      //starts tomcat embedded
      embedded.init();
      embedded.start();
      wasStarted = true;
   }

   protected void stopTomcatEmbedded() throws LifecycleException, org.apache.catalina.LifecycleException
   {
      embedded.stop();
   }
}
