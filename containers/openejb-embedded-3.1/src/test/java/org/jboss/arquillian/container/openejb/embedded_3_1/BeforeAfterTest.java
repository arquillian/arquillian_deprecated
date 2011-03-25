/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.arquillian.container.openejb.embedded_3_1;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.container.openejb.embedded_3_1.ejb.EchoBean;
import org.jboss.arquillian.container.openejb.embedded_3_1.ejb.EchoLocalBusiness;
import org.jboss.arquillian.container.openejb.embedded_3_1.ejb.SimpleBean;
import org.jboss.arquillian.container.openejb.embedded_3_1.ejb.SimpleLocalBusiness;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the execution of BeforeClass and Before
 * annotated methods.  This is essentially testing that the profiles
 * are setup correctly for OpenEJB.
 * 
 * @author David Allen
 *
 */
@RunWith(Arquillian.class)
public class BeforeAfterTest
{
   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Define the deployment
    */
   @Deployment
   public static JavaArchive createDeployment()
   {
      return ShrinkWrap.create(JavaArchive.class, "test.jar").addClasses(EchoLocalBusiness.class, EchoBean.class, BeforeAfterTest.class);
   }

   private static boolean beforeClassExecuted = false;
   private boolean beforeExecuted = false;
   
   @BeforeClass
   public static void beforeClass()
   {
      beforeClassExecuted = true;
   }
   
   @Before
   public void before()
   {
      beforeExecuted = true;
   }
   
   @Test
   public void testAllBeforeMethodsExecuted()
   {
      Assert.assertTrue(beforeClassExecuted);
      Assert.assertTrue(beforeExecuted);
   }
}
