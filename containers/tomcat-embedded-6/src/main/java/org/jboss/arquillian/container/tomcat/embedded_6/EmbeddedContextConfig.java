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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletContext;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.xml.sax.InputSource;

/**
 * A custom {@link ContextConfig} for use in the Embedded Tomcat
 * container integration for Arquillian.
 *
 * <p>This configuration adds processing of the META-INF/context.xml
 * descriptor in the web application root when the context is started.
 * This implementation also marks an unpacked WAR for deletion when
 * the context is stopped.</p>
 *
 * @author Dan Allen
 */
public class EmbeddedContextConfig extends ContextConfig
{
   /**
    * Override as a hook to process the application context configuration.
    */
   @Override
   protected void defaultWebConfig()
   {
      applicationContextConfig();
      super.defaultWebConfig();
   }

   /**
    * Process the META-INF/context.xml descriptor in the web application root.
    * This descriptor is not processed when a webapp is added programmatically through a StandardContext
    */
   protected void applicationContextConfig()
   {
      ServletContext servletContext = context.getServletContext();
      InputStream stream = servletContext.getResourceAsStream("/" + Constants.ApplicationContextXml);
      if (stream == null)
      {
         return;
      }
      // this bad-practice synchronization is inherited
      synchronized (contextDigester)
      {
         URL url = null;
         try
         {
            url = servletContext.getResource("/" + Constants.ApplicationContextXml);
         }
         catch (MalformedURLException e)
         {
            throw new AssertionError("/" + Constants.ApplicationContextXml + " should not be considered a malformed URL");
         }
         InputSource is = new InputSource(url.toExternalForm());
         is.setByteStream(stream);
         contextDigester.push(context);
         try
         {
            contextDigester.parse(is);
         }
         catch (Exception e)
         {
            ok = false;
            log.error("Parse error in context.xml for " + context.getName(), e);
         }
         finally
         {
            contextDigester.reset();
            try
            {
               if (stream != null)
               {
                  stream.close();
               }
            }
            catch (IOException e)
            {
               log.error("Error closing context.xml for " + context.getName(), e);
            }
         }
      }
      log.debug("Done processing " + Constants.ApplicationContextXml + " descriptor");
   }

   /**
    * Overridde to assign an internal field that will trigger the removal
    * of the unpacked WAR when the context is closed.
    */
   @Override
   protected void fixDocBase() throws IOException
   {
      super.fixDocBase();
      // If this field is not null, the unpacked WAR is removed when
      // the context is closed. This is normally used by the antiLocking
      // feature, though it should have been the normal behavior, at
      // least for an embedded container.
      originalDocBase = context.getDocBase();
   }

}
