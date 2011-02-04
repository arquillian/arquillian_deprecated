/*
 * JBoss, Community-driven Open Source Middleware
 * Copyright 2010, JBoss by Red Hat, Inc., and individual contributors
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
package org.jboss.arquillian.container.openwebbeans.embedded_1;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;

import org.apache.webbeans.corespi.scanner.AbstractMetaDataDiscovery;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.impl.base.asset.ClassAsset;

/**
 * A ScannerService implementation that processes a ShrinkWrap bean archive
 *
 * <p>Arquillian supplies an in-memory ShrinkWrap archive which the test class
 * is to use to load classes and resources. This implementation of the
 * OpenWebBeans ScannerService SPI looks for the presence of a
 * /META-INF/beans.xml in the ShrinkWrap archive. If present, it registers the
 * location with the OpenWebBeans container, then proceeds to retrieve classes
 * from that archive and pass them to the AnnotationDB to be scanned and
 * processed as managed bean classes.</p>
 *
 * @author <a href="mailto:dan.allen@mojavelinux.com">Dan Allen</a>
 */
public class ShrinkWrapMetaDataDiscovery extends AbstractMetaDataDiscovery
{
   private Archive<?> archive;

   public ShrinkWrapMetaDataDiscovery(Archive<?> archive)
   {
      super();
      this.archive = archive;
   }

   @Override
   protected void configure() throws Exception
   {
      Map<ArchivePath, Node> beansXmls = archive.getContent(Filters.include("/META-INF/beans.xml"));
	  boolean beansXmlPresent = false;
      for (final Map.Entry<ArchivePath, Node> entry : beansXmls.entrySet())
      {
         try
         {
            addWebBeansXmlLocation(
                  new URL(null, "archive:/" + entry.getKey().get(), new URLStreamHandler()
                  {
                     @Override
                     protected java.net.URLConnection openConnection(URL u) throws java.io.IOException
                     {
                        return new URLConnection(u)
                        {
                           @Override
                           public void connect() throws IOException {}

                           @Override
                           public InputStream getInputStream() throws IOException
                           {
                              return entry.getValue().getAsset().openStream();
                           }
                        };
                     };
                  }));
		    beansXmlPresent = true;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

	  if (beansXmlPresent)
	  {
         Map<ArchivePath, Node> classes = archive.getContent(Filters.include(".*\\.class"));
         for (Map.Entry<ArchivePath, Node> classEntry : classes.entrySet())
         {
            if (classEntry.getValue().getAsset() instanceof ClassAsset)
            {
               try
               {
                  getAnnotationDB().scanClass(classEntry.getValue().getAsset().openStream());
               }
               catch (Exception e)
               {
                  e.printStackTrace();
               }
            }
         }
	  }
   }
}
