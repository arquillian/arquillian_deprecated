/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.glassfish.embedded_3_1.app;

import javax.ejb.EJB;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JBossEmbeddedIntegrationTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Ignore // GlassFish bug
@RunWith(Arquillian.class)
public class IntegrationEarTestCase
{
   @Deployment
   public static EnterpriseArchive createDeployment() throws Exception 
   {
      JavaArchive ejb = ShrinkWrap.create(JavaArchive.class)
                     .addClasses(
                           NoInterfaceEJB.class,
                           NameProvider.class)
                     .addManifestResource(EmptyAsset.INSTANCE, "beans.xml");
      
      return ShrinkWrap.create(EnterpriseArchive.class)
//                  .setApplicationXML(new StringAsset(
//                        Descriptors.create(ApplicationDescriptor.class)
//                           .version("6")
//                           .ejbModule(ejb.getName())
//                           .webModule("test.war", "/test")
//                        .exportAsString()))
                  .addModule(ejb);
   }
   
   @EJB
   private NoInterfaceEJB bean;
   
   @Test
   public void shouldBeAbleToInjectEJBAsInstanceVariable() throws Exception 
   {
      Assert.assertNotNull(
            "Verify that the Bean has been injected",
            bean);
      
      Assert.assertEquals("Arquillian", bean.getName());
   }
}
