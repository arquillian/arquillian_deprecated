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
package org.jboss.arquillian.prototyping.context;

import java.util.logging.Logger;

import javax.ejb.EJBAccessException;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.openejb.ejb.EchoBean;
import org.jboss.arquillian.openejb.ejb.EchoLocalBusiness;
import org.jboss.arquillian.prototyping.context.api.Properties;
import org.jboss.arquillian.prototyping.context.api.Property;
import org.jboss.arquillian.prototyping.context.api.openejb.OpenEJBArquillianContext;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that integration with the backing container via 
 * {@link OpenEJBArquillianContext} is in place as contracted
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class OpenEJBAuthenticatedJndiContextTestCase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(OpenEJBAuthenticatedJndiContextTestCase.class.getName());

   /**
    * JNDI Name that OpenEJB will assign to our deployment
    */
   private static final String JNDI_NAME = "EchoBeanLocal";

   /**
    * User who is in role "Administrator"
    */
   private static final String ADMIN_USER_NAME = "admin";

   /**
    * Password of an admin user
    */
   private static final String ADMIN_PASSWORD = "adminPassword";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Define the SLSB Deployment for this test
    */
   @Deployment
   public static JavaArchive createDeployment()
   {
      return ShrinkWrap.create("slsb.jar", JavaArchive.class).addClasses(EchoLocalBusiness.class, EchoBean.class);
   }

   /**
    * Here we test typesafe injection coupled with some context properties;
    * OpenEJB has been configured with security in users.properties and 
    * groups.properties on the test classpath.  If this doesn't work, we'll either get
    * an error during injection (during login) or when we try to get at a privileged 
    * method in the test EJB.
    */
   @Properties(
   {@Property(key = Context.SECURITY_PRINCIPAL, value = ADMIN_USER_NAME),
         @Property(key = Context.SECURITY_CREDENTIALS, value = ADMIN_PASSWORD)})
   @Inject
   private Context namingContext;

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that we can invoke upon a bean obtained via a secured logic
    * and access restricted methods
    */
   @Test
   public void authenticatedInvocation() throws NamingException
   {
      // Look up the EJB though the authenticated Context
      final EchoLocalBusiness bean = (EchoLocalBusiness) namingContext.lookup(JNDI_NAME);

      // Invoke and test
      final String expected = "Authenticated Invocation";
      final String actual;
      try
      {
         actual = bean.securedEcho(expected);
      }
      catch (final EJBAccessException e)
      {
         Assert.fail("Should have been able to access secured method via authenticated JNDI Context, but got: " + e);
         return;
      }
      Assert.assertSame("Value was not as expected", expected, actual);
   }
}
