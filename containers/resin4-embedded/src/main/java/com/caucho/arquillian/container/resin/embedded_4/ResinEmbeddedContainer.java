/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Dominik Dorn
 */
package com.caucho.arquillian.container.resin.embedded_4;


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
