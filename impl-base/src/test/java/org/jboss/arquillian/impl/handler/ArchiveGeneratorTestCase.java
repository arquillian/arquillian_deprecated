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
package org.jboss.arquillian.impl.handler;

import junit.framework.Assert;

import org.jboss.arquillian.impl.DeploymentGenerator;
import org.jboss.arquillian.impl.context.ClassContext;
import org.jboss.arquillian.impl.context.SuiteContext;
import org.jboss.arquillian.spi.DeploymentPackager;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * ArchiveGeneratorHandlerTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ArchiveGeneratorTestCase
{
   @Mock
   private ServiceLoader serviceLoader;
   
   @Mock
   private DeploymentGenerator generator;
   
   @Mock
   private DeploymentPackager packager;

   @Test(expected = IllegalStateException.class)
   public void shouldThrowIllegalStateExceptionOnMissingDeploymentGenerator() throws Exception
   {
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      ArchiveGenerator handler = new ArchiveGenerator();
      handler.callback(context, new ClassEvent(new TestClass(getClass())));
   }

   @Test
   public void shouldGenerateArchive() throws Exception
   {
      final Archive archive = ShrinkWrap.create(JavaArchive.class, "test.jar");
      final TestDeployment deployment = new TestDeployment(archive);
      
      ClassContext context = new ClassContext(new SuiteContext(serviceLoader));
      context.add(ServiceLoader.class, serviceLoader);
      context.add(DeploymentGenerator.class, generator);

      TestClass testClass = new TestClass(getClass());
      Mockito.when(generator.generate(testClass)).thenReturn(deployment);
      Mockito.when(serviceLoader.onlyOne(DeploymentPackager.class)).thenReturn(packager);
      Mockito.when(packager.generateDeployment(context, deployment)).thenReturn(archive);
      
      ArchiveGenerator handler = new ArchiveGenerator();
      handler.callback(context, new ClassEvent(testClass));
      
      Assert.assertNotNull(
            "Should have exported " + Archive.class.getSimpleName(), 
            context.get(Archive.class));
   }
}
