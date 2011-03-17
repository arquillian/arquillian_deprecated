/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.container.jbossas.remote_6;

import javax.annotation.Resource;
import javax.jms.Queue;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.jpa.persistence.PersistenceDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * DescriptorDeploymentTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class DescriptorDeploymentTestCase
{
   private static final String DEP = "deployment";
   
   private static final String TEST_QUEUE_DEF = "" +
   		"<configuration xmlns=\"urn:hornetq\" \n" + 
   		"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
   		"    xsi:schemaLocation=\"urn:hornetq ../schemas/hornetq-jms.xsd \">\n" + 
   		"    \n" + 
   		"    <queue name=\"TestQueue\">\n" + 
   		"        <entry name=\"queues/Test\"/>\n" + 
   		"    </queue>\n" + 
   		"    \n" + 
   		"</configuration>";
   
   @Deployment(order = 1)
   public static Descriptor createQueue() 
   {
      // we have no JMSDescriptor, but importing as any other should export it clean.
      // TODO: create a Generic Descriptor type.
      return Descriptors.importAs(PersistenceDescriptor.class, "test-hornetq-jms.xml").from(TEST_QUEUE_DEF);
   }
   
   @Deployment(name = DEP, order = 2)
   public static JavaArchive createTest()
   {
      return ShrinkWrap.create(JavaArchive.class);
   }
   
   @Resource(mappedName = "queues/Test")
   private Queue queue; 
   
   @Test @OperateOnDeployment(DEP)
   public void shouldHaveInjectedTestQueue()
   {
      Assert.assertNotNull(queue);
   }
}
