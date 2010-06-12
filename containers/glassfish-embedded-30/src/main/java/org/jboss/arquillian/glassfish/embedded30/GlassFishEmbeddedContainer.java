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
package org.jboss.arquillian.glassfish.embedded30;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.admin.cli.resources.AddResources;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.embedded.ContainerBuilder;
import org.glassfish.api.embedded.EmbeddedFileSystem;
import org.glassfish.api.embedded.Server;
import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.glassfish.api.ShrinkwrapReadableArchive;
import org.jvnet.hk2.annotations.Service;

/**
 * GlassFishEmbeddedContainer
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @author Dan Allen
 * @version $Revision: $
 * @see org.glassfish.admin.cli.resources.AddResources
 */
public class GlassFishEmbeddedContainer implements DeployableContainer
{
   public static final String HTTP_PROTOCOL = "http";
   public static final String DEFAULT_ASADMIN_PARAM = "DEFAULT";

   private static final Logger log = Logger.getLogger(GlassFishEmbeddedContainer.class.getName());

   private String target = "server";
   private Server server;

   private GlassFishConfiguration containerConfig;
   
   public GlassFishEmbeddedContainer()
   {
   }
   
   public void setup(Context context, Configuration arquillianConfig)
   {
      containerConfig = arquillianConfig.getContainerConfig(GlassFishConfiguration.class);
      final Server.Builder serverBuilder = new Server.Builder("arquillian-" + System.currentTimeMillis());

      final EmbeddedFileSystem.Builder embeddedFsBuilder = new EmbeddedFileSystem.Builder()
            .instanceRoot(new File(containerConfig.getInstanceRoot()))
            .autoDelete(containerConfig.isAutoDelete());
      if (containerConfig.getDomainXml() != null)
      {
         File domainXmlFile = new File(containerConfig.getDomainXml());
         if (!domainXmlFile.exists() || !domainXmlFile.isFile())
         {
            throw new RuntimeException("File specified in domainXml configuration property does not exist: " +
                  domainXmlFile.getAbsolutePath());
         }
         embeddedFsBuilder.configurationFile(domainXmlFile);
      }
      
      server = serverBuilder.embeddedFileSystem(embeddedFsBuilder.build()).build();

      server.addContainer(ContainerBuilder.Type.all);

      if (containerConfig.getSunResourcesXml() != null)
      {
         File resourcesXmlFile = new File(containerConfig.getSunResourcesXml());
         if (!resourcesXmlFile.exists() || !resourcesXmlFile.isFile())
         {
            throw new RuntimeException("File specified in sunResourcesXml configuration property does not exist: " +
                  resourcesXmlFile.getAbsolutePath());
         }
         try
         {
            // GlassFish's resources XML parser is hardcoded to look for the DTD in this location
            File resourcesDtd = new File(server.getFileSystem().instanceRoot, "lib/dtds/sun-resources_1_4.dtd");
            if (!resourcesDtd.exists())
            {
               resourcesDtd.getParentFile().mkdirs();
               copyWithClose(getClass().getClassLoader().getResourceAsStream("META-INF/sun-resources_1_4.dtd"),
                     new FileOutputStream(resourcesDtd));
            }
            ParameterMap params = new ParameterMap();
            params.add(DEFAULT_ASADMIN_PARAM, containerConfig.getSunResourcesXml());
            {
               executeCommand(AddResources.class.getAnnotation(Service.class).name(), server, params);
            }
         }
         catch (Throwable ex)
         {
            throw new RuntimeException(ex);
         }
      }
   }
   
   public void start(Context context) throws LifecycleException
   {
      try 
      {
         // embedded glassfish automatically binds the first port created to http
         // the documentation, however, is very fuzzy
         server.createPort(containerConfig.getBindHttpPort());
         server.start();
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not start container", e);
      }
   }

   public void stop(Context context) throws LifecycleException
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

   public ContainerMethodExecutor deploy(Context context, Archive<?> archive) throws DeploymentException
   {
      try 
      {
         DeployCommandParameters params = new DeployCommandParameters();
         params.enabled = true;
         params.target = target;
         params.name = createDeploymentName(archive.getName());
         
         server.getDeployer().deploy(
               archive.as(ShrinkwrapReadableArchive.class),
               params);
         
      } 
      catch (Exception e) 
      {
         throw new DeploymentException("Could not deploy " + archive.getName(), e);
      }

      try 
      {
         return new ServletMethodExecutor(
               new URL(
                     HTTP_PROTOCOL,
                     "localhost",
                     containerConfig.getBindHttpPort(),
                     "/")
               );
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not create ContainerMethodExecutor", e);
      }
   }

   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      UndeployCommandParameters params = new UndeployCommandParameters();
      params.target = target;
      params.name = createDeploymentName(archive.getName());
      try 
      {
         server.getDeployer().undeploy(params.name, params);
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not undeploy " + archive.getName(), e);
      }
   }
   
   private String createDeploymentName(String archiveName) 
   {
      return archiveName.substring(0, archiveName.lastIndexOf("."));
   }

   private void executeCommand(String command, Server server, ParameterMap params) throws Throwable
   {
      CommandRunner runner = server.getHabitat().getComponent(CommandRunner.class);
      ActionReport report = server.getHabitat().getComponent(ActionReport.class);
      CommandRunner.CommandInvocation invocation = runner.getCommandInvocation(command, report);
      if (params != null)
      {
         invocation.parameters(params);
      }

      invocation.execute();

      if (report.hasFailures())
      {
         throw report.getFailureCause();
      }
      else
      {
         int i = 1;
         for (MessagePart part : report.getTopMessagePart().getChildren())
         {
            log.info(command + " command result (" + i++ + "): " + part.getMessage());

         }
      }
   }

   /**
    * Copies the contents from an InputStream to an OutputStream and closes both streams.
    */
   public static void copyWithClose(InputStream input, OutputStream output) throws IOException
   {
      try
      {
         final byte[] buffer = new byte[4096];
         int read = 0;
         while ((read = input.read(buffer)) != -1)
         {
            output.write(buffer, 0, read);
         }

         output.flush();
      }
      finally
      {
         try
         {
            input.close();
         }
         catch (final IOException ignore)
         {
            if (log.isLoggable(Level.FINER))
            {
               log.finer("Could not close stream due to: " + ignore.getMessage() + "; ignoring");
            }
         }
         try
         {
            output.close();
         }
         catch (final IOException ignore)
         {
            if (log.isLoggable(Level.FINER))
            {
               log.finer("Could not close stream due to: " + ignore.getMessage() + "; ignoring");
            }
         }
      }
   }
}
