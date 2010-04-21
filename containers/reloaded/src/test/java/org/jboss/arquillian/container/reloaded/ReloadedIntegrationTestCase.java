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
package org.jboss.arquillian.container.reloaded;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.bootstrap.api.mc.server.MCServer;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that deployments into the {@link MCServer}
 * and Virtual Deployment Framework (ie. JBoss Reloaded)
 * work through the Arquillian lifecycle
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class ReloadedIntegrationTestCase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(ReloadedIntegrationTestCase.class.getName());

   /**
    * Define the deployment
    */
   @Deployment
   public static JavaArchive createDeployment()
   {
      // Construct a test JAR to install the Lifecycle POJO
      final Asset deploymentXmlAsset = new Asset()
      {

         public InputStream openStream()
         {
            return new ByteArrayInputStream(new String(
                  "<deployment xmlns=\"urn:jboss:bean-deployer:2.0\"><bean name=\"LifecyclePojo\" class=\""
                        + LifecyclePojo.class.getName() + "\" /></deployment>").getBytes());
         }
      };
      final JavaArchive testJar = Archives.create("pojo.jar", JavaArchive.class).addClass(LifecyclePojo.class).add(
            deploymentXmlAsset, ArchivePaths.create("pojo-jboss-beans.xml"));
      log.info(testJar.toString(true));
      return testJar;
   }

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * POJO to be injected (as installed from MC)
    */
   private LifecyclePojo pojo;

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures injection into the test case takes place as expected
    */
   @Test
   public void pojoInstalledAndInjected()
   {
      Assert.assertNotNull("POJO should have been injected from MC", pojo);
      Assert.assertEquals("MC Should have activated POJO in STARTED state", LifecyclePojo.State.STARTED, pojo.state);
      log.info(pojo + " is in state: " + pojo.state);
   }

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Sets the {@link LifecyclePojo} from MC
    */
   @Inject
   public void setPojo(final LifecyclePojo pojo)
   {
      assert pojo != null : "POJO should not be null";
      this.pojo = pojo;
   }

}
