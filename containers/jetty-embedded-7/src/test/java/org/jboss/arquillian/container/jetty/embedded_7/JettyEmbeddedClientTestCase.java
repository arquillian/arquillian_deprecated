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
package org.jboss.arquillian.container.jetty.embedded_7;

import static org.jboss.arquillian.api.RunModeType.AS_CLIENT;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Client test case for the Jetty Embedded 7 container
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author Dan Allen
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
@Run(AS_CLIENT)
public class JettyEmbeddedClientTestCase
{
   private static final Logger log = Logger.getLogger(JettyEmbeddedClientTestCase.class.getName());
   
   /**
    * Deployment for the test
    */
   @Deployment
   public static WebArchive getTestArchive()
   {
      final WebArchive war = ShrinkWrap.create(WebArchive.class, "client-test.war")
         .addClass(TestServlet.class)
         .setWebXML("client-web.xml");
      log.info(war.toString(true));
      return war;
   }

   @Test
   public void shouldBeAbleToInvokeServletInDeployedWebApp() throws Exception
   {
      String body = readAllAndClose(
            new URL("http://localhost:9595/client-test" + TestServlet.URL_PATTERN).openStream());
      
      Assert.assertEquals(
            "Verify that the servlet was deployed and returns expected result",
            TestServlet.MESSAGE,
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
