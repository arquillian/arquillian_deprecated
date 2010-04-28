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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.openejb.ejb.EchoBean;
import org.jboss.arquillian.openejb.ejb.EchoLocalBusiness;
import org.jboss.arquillian.prototyping.context.api.ArquillianContext;
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
public class OpenEJBArquillianContextTestCase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(OpenEJBArquillianContextTestCase.class.getName());

   /**
    * JNDI Name that OpenEJB will assign to our deployment
    */
   private static final String JNDI_NAME = "EchoBeanLocal";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * TODO: We don't really need a deployment
    */
   @Deployment
   public static JavaArchive createDeployment()
   {
      return ShrinkWrap.create("slsb.jar", JavaArchive.class).addClasses(EchoLocalBusiness.class, EchoBean.class);
   }

   /**
    * The hook to the ARQ container, and by extension, OpenEJB
    */
   @Inject
   private OpenEJBArquillianContext arquillianContext;

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that we may inject an {@link ArquillianContext}
    * into the test
    */
   @Test
   public void injectArquillianContext()
   {
      Assert.assertNotNull("Arquillian context should have been injected", arquillianContext);
   }

   /**
    * Ensures we can get at OpenEJB deployment metadata
    * from the {@link OpenEJBArquillianContext} 
    */
   @Test
   public void deploymentMetadata()
   {
      final String ejbName = arquillianContext.getDeploymentMetadata().ejbJars.get(0).enterpriseBeans.get(0).ejbName;
      log.info("Got EJB Name: " + ejbName);
      Assert.assertEquals("Did not obtain correct EJB name from deployment metadata", EchoBean.class.getSimpleName(),
            ejbName);
   }

   /**
    * Ensures we can create an OpenEJB-specific JNDI {@link Context} via the 
    * {@link OpenEJBArquillianContext} 
    */
   @Test
   public void programmaticNamingContext() throws NamingException
   {
      final Context context = arquillianContext.get(Context.class);
      Assert.assertNotNull("Should be able to look up EJB via naming context obtained from Arquillian context", context
            .lookup(JNDI_NAME));
   }

   /**
    * Ensures we can create an OpenEJB-specific JNDI {@link Context} via the 
    * {@link OpenEJBArquillianContext} which supports/respects context properties
    */
   @Test
   public void programmaticNamingContextWithProperties() throws NamingException
   {
      final Map<String, Object> props = new HashMap<String, Object>();
      props.put(Context.SECURITY_PRINCIPAL, "testuser");
      props.put(Context.SECURITY_CREDENTIALS, "testpassword");
      try
      {
         // This should fail on construction, because we haven't a matching user/pass configured in OpenEJB
         arquillianContext.get(Context.class, props);
      }
      catch (final RuntimeException re)
      {
         // Validates that the props we passed in were respected when making the naming context
         Assert.assertEquals(AuthenticationException.class, re.getCause().getClass());
         return;
      }
      TestCase.fail("Should have obtained exception on logging in with bad user/pass config");

   }
}
