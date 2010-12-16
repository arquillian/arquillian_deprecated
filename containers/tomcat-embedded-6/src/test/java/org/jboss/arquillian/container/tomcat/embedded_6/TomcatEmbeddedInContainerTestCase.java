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

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.dependencies.Dependencies;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that Tomcat deployments into the Tomcat server work through the
 * Arquillian lifecycle
 * 
 * @author Dan Allen
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class TomcatEmbeddedInContainerTestCase
{
    private static final String HELLO_WORLD_URL = "http://localhost:8888/test2/Test";

    // -------------------------------------------------------------------------------------||
    // Class Members -----------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(TomcatEmbeddedInContainerTestCase.class.getName());

    // -------------------------------------------------------------------------------------||
    // Instance Members --------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Define the deployment
     */
    @Deployment
    public static WebArchive createTestArchive()
   {
        return ShrinkWrap.create(WebArchive.class, "test2.war")
         .addClasses(TestServlet.class, TestBean.class)
         .addLibraries(Dependencies.artifact("org.jboss.weld.servlet:weld-servlet:1.1.0.Beta2").resolve())
         .addWebResource(EmptyAsset.INSTANCE, "beans.xml")
         .addResource("in-container-context.xml", "META-INF/context.xml")
         .setWebXML("in-container-web.xml");
    }

    // -------------------------------------------------------------------------------------||
    // Tests -------------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

   @Resource(name = "name") String name;

   @Inject TestBean testBean;

    /**
     * Ensures the {@link HelloWorldServlet} returns the expected response
     */
    @Test
    public void shouldBeAbleToInjectMembersIntoTestClass()
   {
      log.info("Name: " + name);
      Assert.assertEquals("Tomcat", name);
      Assert.assertNotNull(testBean);
      Assert.assertEquals("Tomcat", testBean.getName());
    }

   @Test
   public void shouldBeAbleToInvokeServletInDeployedWebApp() throws Exception
   {
        // Define the input and expected outcome
        final String expected = "hello";

        URL url = new URL(HELLO_WORLD_URL);
        InputStream in = url.openConnection().getInputStream();

        byte[] buffer = new byte[10000];
        int len = in.read(buffer);
        String httpResponse = "";
        for (int q = 0; q < len; q++)
      {
            httpResponse += (char) buffer[q];
      }

        // Test
        Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
        log.info("Got expected result from Http Servlet: " + httpResponse);
   }
}
