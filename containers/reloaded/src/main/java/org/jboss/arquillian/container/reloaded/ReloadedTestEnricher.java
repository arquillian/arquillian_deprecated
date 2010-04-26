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

import java.lang.reflect.Method;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.spi.event.container.ContainerEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.beans.info.spi.BeanAccessMode;
import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.bootstrap.api.mc.server.MCServer;
import org.jboss.kernel.spi.dependency.KernelController;

/**
 * {@link TestEnricher} implementation which provides all injection
 * and service features of the Microcontainer to the test instance
 * by installing it into MC.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ReloadedTestEnricher implements TestEnricher
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * MC Bind name under which the test will be installed
    * TODO: Should be private, this wider access is needed by the {@link ReloadedContainer}
    * @see http://community.jboss.org/thread/150796?tstart=0
    */
   static final String BIND_NAME_TEST = "org.jboss.arquillian.CurrentTest";

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.TestEnricher#enrich(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   public void enrich(final Context context, final Object testCase)
   {
      // Obtain the server as set from the container
      final MCServer server = context.get(MCServer.class);
      assert server != null : "MC Server was not set by the container";

      // Get the Controller
      final KernelController controller = server.getKernel().getController();

      // Install the test instance itself into MC (so injections may be honored)
      final BeanMetaDataBuilder bmdb = BeanMetaDataBuilderFactory.createBuilder(BIND_NAME_TEST,
            testCase.getClass().getName()).setAccessMode(BeanAccessMode.ALL);
      try
      {
         controller.install(bmdb.getBeanMetaData(), testCase);
         context.getParentContext().register(BeforeUnDeploy.class, new TestCaseUnInstaller());
      }
      catch (final Throwable e)
      {
         throw new RuntimeException("Could not enrich " + testCase + " by installing the instance into MC", e);
      }
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.TestEnricher#resolve(org.jboss.arquillian.spi.Context, java.lang.reflect.Method)
    */
   public Object[] resolve(final Context context, final Method method)
   {
      return new Object[method.getParameterTypes().length];
   }

   /**
    * Uninstall the installed test case from the MCServer before undeploying. 
    *
    * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
    * @version $Revision: $
    */
   private static class TestCaseUnInstaller implements EventHandler<ContainerEvent>
   {
      public void callback(final Context context, final ContainerEvent event) throws Exception
      {
         context.get(MCServer.class).getKernel().getController().uninstall(ReloadedTestEnricher.BIND_NAME_TEST);
      }
   }
}
