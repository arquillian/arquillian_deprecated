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
package org.jboss.arquillian.impl.client.container;

import org.jboss.arquillian.impl.AbstractManagerTestBase;
import org.jboss.arquillian.impl.client.container.event.StartManagedContainers;
import org.jboss.arquillian.impl.client.container.event.StopManagedContainers;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;
import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * ContainerRestarterTestCase
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(MockitoJUnitRunner.class)
public class ContainerRestarterTestCase extends AbstractManagerTestBase
{
   @Override
   protected void addExtensions(ManagerBuilder builder)
   {
      builder.extension(ContainerRestarter.class);
   }

   @Test
   public void shouldRestartContainerForEveryX() throws Exception 
   {
      ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class)
            .engine().maxDeploymentsBeforeRestart(5);

      bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);
      
      for(int i = 0; i < 10; i++)
      {
         fire(new BeforeClass(getClass()));
      }

      assertEventFired(StartManagedContainers.class, 2);
      assertEventFired(StopManagedContainers.class, 2);
   }
   
   @Test
   public void shouldNotForceRestartIfMaxDeploymentsNotSet() throws Exception
   {
      ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class)
            .engine();

      bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);
      
      for(int i = 0; i < 10; i++)
      {
         fire(new BeforeClass(getClass()));
      }

      assertEventFired(StartManagedContainers.class, 0);
      assertEventFired(StopManagedContainers.class, 0);
   }
}
