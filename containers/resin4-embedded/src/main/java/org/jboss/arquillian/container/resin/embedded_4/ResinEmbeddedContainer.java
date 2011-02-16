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
import org.jboss.arquillian.protocol.servlet_3.ServletMethodExecutor;
import org.jboss.arquillian.spi.*;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

/**
 * <p>Resin4 Embedded container for the Arquillian project.</p>
 *
 * @author Dominik Dorn
 * @version $Revision: $
 */
public class ResinEmbeddedContainer implements DeployableContainer
{
   public static final String HTTP_PROTOCOL = "http";

   private static final Logger log = Logger.getLogger(ResinEmbeddedContainer.class.getName());

   private ResinEmbed server;

   private ResinEmbeddedConfiguration containerConfig;
   
   public ResinEmbeddedContainer()
   {
   }
   
   public void setup(Context context, Configuration arquillianConfig)
   {
      containerConfig = arquillianConfig.getContainerConfig(ResinEmbeddedConfiguration.class);
       arquillianConfig.setDeploymentExportPath("/tmp/arquillian");
   }
   
   public void start(Context context) throws LifecycleException
   {
      try 
      {
          server = new ResinEmbed();
          server.addPort(new HttpEmbed(containerConfig.getBindHttpPort()));
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
         log.info("Stopping Resin Embedded Server [id:" + server.hashCode() + "]");
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
          String basePath = "/tmp/resinarquillian";

          File base = new File(basePath);

          File warFile = new File(basePath, archive.getName());

          base.mkdirs();
          warFile.delete();

          log.finer("deploying");
          log.finer("archive.getName() = " + archive.getName());
//          new File("/tmp/" + archive.getName()).delete();
          ZipExporter exporter = archive.as(ZipExporter.class);
//          exporter.exportZip(new File("/tmp/" + archive.getName()));
          exporter.exportZip(warFile.getAbsoluteFile());

          WebAppEmbed webApp = new WebAppEmbed();
          webApp.setContextPath("/test");
          webApp.setRootDirectory(base + "/tmp");
          webApp.setArchivePath(warFile.getAbsolutePath() );
          log.info("adding webapp to server");
          server.addWebApp(webApp);
          server.start();
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
                     containerConfig.getBindAddress(),
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
//       new File("/tmp"+archive.getName()).delete();
//      WebAppContext wctx = context.get(WebAppContext.class);
//      if (wctx != null)
//      {
//         try
//         {
//            wctx.stop();
//         }
//         catch (Exception e)
//         {
//            e.printStackTrace();
//            log.severe("Could not stop context " + wctx.getContextPath() + ": " + e.getMessage());
//         }
//         ((HandlerCollection) server.getHandler()).removeHandler(wctx);
//      }
   }

}
