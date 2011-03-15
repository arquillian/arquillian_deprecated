/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import javax.ejb.EJB;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.container.openejb.embedded_3_1.ejb.DataBean;
import org.jboss.arquillian.container.openejb.embedded_3_1.ejb.DataLocalBusiness;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for the configurability of OpenEJB containers via Arquillian and
 * OpenEJB configuration files.
 * 
 * @author David Allen
 *
 */
@RunWith(Arquillian.class)
public class OpenEJBConfigTestCase
{
   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Define the deployment
    */
   @Deployment
   public static JavaArchive createDeployment()
   {
      return ShrinkWrap.create(JavaArchive.class, "slsb.jar").addClasses(DataLocalBusiness.class, DataBean.class);
   }

   /**
    * The EJB proxy used for invocations
    */
   @EJB(mappedName="dataBeans/DataLocalBusinessLocal")
   private DataLocalBusiness bean;

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that the configuration specified in arquillian.xml
    * is being used for this test.  (ARQ-379)
    */
   @Test
   public void testConfiguration()
   {
      Assert.assertTrue(bean.isDataSourceAvailable());
   }

}
