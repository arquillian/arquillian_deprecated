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
package org.jboss.arquillian.container.glassfish.remote_3;

import static org.jboss.arquillian.api.RunModeType.AS_CLIENT;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.annotation.WebServlet;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that the an Arquillian test can be run in client
 * mode with a the JSR 88 Remote container for GlassFish.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author Dan Allen
 */
@RunWith(Arquillian.class)
@Run(AS_CLIENT)
public class GlassFishJSR88RemoteContainerEARTestCase
{
   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(GlassFishJSR88RemoteContainerEARTestCase.class.getName());
   
   /**
    * Deployment for the test
    * @return
    */
   @Deployment
   public static Archive<?> getTestArchive()
   {
      final WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
            .addClasses(GreeterServlet.class);
      final JavaArchive ejb = ShrinkWrap.create(JavaArchive.class, "test.jar")
            .addClass(Greeter.class);
      final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "test.ear")
            .setApplicationXML("application.xml")
            .addModule(war)
            .addModule(ejb);
      log.info(ear.toString(true));
      return ear;
   }

   @Test
   public void shouldBeAbleToDeployWebArchive() throws Exception
   {
      String servletPath = GreeterServlet.class.getAnnotation(WebServlet.class).urlPatterns()[0];
      String body = readAllAndClose(new URL("http://localhost:8080/test" + servletPath).openStream());
      
      Assert.assertEquals(
            "Verify that the servlet was deployed and returns expected result",
            "hello",
            body);
   }
   
   private String readAllAndClose(InputStream is) throws Exception 
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try
      {
         int read;
         while( (read = is.read()) != -1)
         {
            out.write(read);
         }
      }
      finally 
      {
         try { is.close(); } catch (Exception e) { }
      }
      return out.toString();
   }
}
