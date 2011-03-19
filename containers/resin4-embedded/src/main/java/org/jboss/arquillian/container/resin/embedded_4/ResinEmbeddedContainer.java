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
package org.jboss.arquillian.container.resin.embedded_4;


import com.caucho.resin.HttpEmbed;
import com.caucho.resin.ResinEmbed;
import com.caucho.resin.WebAppEmbed;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * <p>Resin4 Embedded container for the Arquillian project.</p>
 *
 * @author Dominik Dorn
 * @author ales.justin@jboss.org
 * @version $Revision: $
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class ResinEmbeddedContainer implements DeployableContainer<ResinEmbeddedConfiguration>
{
   public static final String HTTP_PROTOCOL = "http";

   private static final Logger log = Logger.getLogger(ResinEmbeddedContainer.class.getName());

   private ResinEmbed server;

   private ResinEmbeddedConfiguration containerConfig;

   private File base;

   public Class<ResinEmbeddedConfiguration> getConfigurationClass()
   {
      return ResinEmbeddedConfiguration.class;
   }

   public void setup(ResinEmbeddedConfiguration configuration)
   {
      containerConfig = configuration;
   }

   public void start() throws LifecycleException
   {
      String basePath = "/target/resin4_arquillian";
      base = new File(basePath);
      if (base.exists() == false)
         base.mkdirs();

      try
      {
         server = new ResinEmbed();
         server.setRootDirectory(basePath);
         server.addPort(new HttpEmbed(containerConfig.getBindHttpPort()));
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not create Resin4 container", e);
      }
   }

   public void stop() throws LifecycleException
   {
      try
      {
         log.info("Destroying Resin Embedded Server [id:" + server.hashCode() + "]");
         server.destroy();

         deleteRecursively(base);
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not destroy Resin4 container", e);
      }
   }

   public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException
   {
      try
      {
         File warFile = new File(base, archive.getName());
         if (warFile.exists())
            warFile.delete();

         log.finer("Web archive = " + archive.getName());
         ZipExporter exporter = archive.as(ZipExporter.class);
         exporter.exportTo(warFile.getAbsoluteFile());

         WebAppEmbed webApp = new WebAppEmbed();
         webApp.setContextPath("/test");
         webApp.setRootDirectory(base + "/tmp");
         webApp.setArchivePath(warFile.getAbsolutePath());
         log.info("Adding webapp to server: " + webApp);
         server.addWebApp(webApp);

         server.start();
      }
      catch (Exception e)
      {
         throw new DeploymentException("Could not deploy " + archive.getName(), e);
      }

      try
      {
         return new ProtocolMetaData();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create ContainerMethodExecutor", e);
      }
   }

   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 3.0");
   }

   public void undeploy(Archive<?> archive) throws DeploymentException
   {
      server.stop();
   }

   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      // TODO
   }

   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      // TODO
   }

   static void deleteRecursively(File file) throws IOException
   {
      if (file.isDirectory())
         deleteDirectoryContents(file);

      if (file.delete() == false)
      {
         throw new IOException("Failed to delete " + file);
      }
   }

   static void deleteDirectoryContents(File directory) throws IOException
   {
      File[] files = directory.listFiles();
      if (files == null)
         throw new IOException("Error listing files for " + directory);

      for (File file : files)
      {
         deleteRecursively(file);
      }
   }
}
