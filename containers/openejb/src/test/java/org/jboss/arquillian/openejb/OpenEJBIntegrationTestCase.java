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
package org.jboss.arquillian.openejb;

import java.util.logging.Logger;

import javax.ejb.EJB;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.openejb.ejb.EchoBean;
import org.jboss.arquillian.openejb.ejb.EchoLocalBusiness;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that EJB deployments into the OpenEJB server
 * work through the Arquillian lifecycle
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class OpenEJBIntegrationTestCase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(OpenEJBIntegrationTestCase.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Define the deployment
    */
   @Deployment
   public static JavaArchive createDeployment()
   {
      return ShrinkWrap.create("slsb.jar", JavaArchive.class).addClasses(EchoLocalBusiness.class, EchoBean.class);
   }

   /**
    * The EJB proxy used for invocations
    */
   @EJB
   private EchoLocalBusiness bean;

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures the {@link EchoBean} returns the expected 
    * response via pass-by-reference semantics
    */
   @Test
   public void testEchoBean()
   {
      // Define the input and expected outcome
      final String expected = "Word up.";

      // Invoke upon the proxy
      final String received = bean.echo(expected);

      // Test
      Assert.assertEquals("Expected output was not equal by value", expected, received);
      Assert.assertTrue("Expected output was not equal by reference", expected == received);
      log.info("Got expected result from EJB: " + received);
   }
}
