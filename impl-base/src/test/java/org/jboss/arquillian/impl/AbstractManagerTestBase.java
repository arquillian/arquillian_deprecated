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
package org.jboss.arquillian.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.jboss.arquillian.impl.core.ManagerBuilder;
import org.jboss.arquillian.impl.core.ManagerImpl;
import org.jboss.arquillian.impl.core.UncheckedThrow;
import org.jboss.arquillian.impl.core.context.ClassContextImpl;
import org.jboss.arquillian.impl.core.context.ContainerContextImpl;
import org.jboss.arquillian.impl.core.context.DeploymentContextImpl;
import org.jboss.arquillian.impl.core.context.SuiteContextImpl;
import org.jboss.arquillian.impl.core.context.TestContextImpl;
import org.jboss.arquillian.impl.core.spi.Manager;
import org.jboss.arquillian.impl.core.spi.context.ClassContext;
import org.jboss.arquillian.impl.core.spi.context.ContainerContext;
import org.jboss.arquillian.impl.core.spi.context.Context;
import org.jboss.arquillian.impl.core.spi.context.DeploymentContext;
import org.jboss.arquillian.impl.core.spi.context.SuiteContext;
import org.jboss.arquillian.impl.core.spi.context.TestContext;
import org.jboss.arquillian.spi.core.Injector;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ApplicationScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.junit.After;
import org.junit.Before;

/**
 * AbstractManagerTestBase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class AbstractManagerTestBase
{
   private ManagerImpl manager;
      
   @Before
   public final void create() 
   {
      ManagerBuilder builder = ManagerBuilder.from();
      addContexts(builder);
      builder.extension(EventRegisterObserver.class);
      addExtensions(builder);
      manager = builder.create();
      
      manager.resolve(Injector.class).inject(this);
      startContexts(manager);
   }
   
   @After
   public final void destory()
   {
      manager.shutdown();
   }
   
   public ManagerImpl getManager()
   {
      return manager;
   }

   //-------------------------------------------------------------------------------------||
   // Assertions and Helper operations ----------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public final void fire(Object event)
   {
      manager.fire(event);
   }
   
   public final <T> void bind(Class<? extends Annotation> scope, Class<T> type, T instance)
   {
      manager.bind(scope, type, instance);
   }

   public final void assertEventFired(Class<?> type)
   {
      Assert.assertNotNull(
            "Event " + type.getName() + " should have been fired", 
            manager.resolve(EventRegister.class).getCount(type));
   }

   public final void assertEventFired(Class<?> type, Integer count)
   {
      Assert.assertEquals(
            "The event of exact type " + type.getName() + " should have been fired",
            count,
            manager.resolve(EventRegister.class).getCount(type));
   }

   public final void assertEventFiredTyped(Class<?> type, Integer count)
   {
      Assert.assertEquals(
            "The event of assiganble type to " + type.getName() + " should have been fired",
            count,
            manager.resolve(EventRegister.class).getCountTyped(type));
   }

   public final void assertEventFiredInContext(Class<?> type, Class<? extends Context> activeContext)
   {
         Assert.assertTrue(
               "Event " + type.getName() + " should have been fired within context " + activeContext.getName(),
               manager.resolve(EventRegister.class).wasActive(type, activeContext));
   }

   //-------------------------------------------------------------------------------------||
   // Extendables ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   
   protected void addContexts(ManagerBuilder builder)
   {
      builder.context(SuiteContextImpl.class)
            .context(ClassContextImpl.class)
            .context(TestContextImpl.class)
            .context(ContainerContextImpl.class)
            .context(DeploymentContextImpl.class);
   }

   protected abstract void addExtensions(ManagerBuilder builder);

   protected void startContexts(Manager manager)
   {
      manager.getContext(SuiteContext.class).activate();
      manager.getContext(ClassContext.class).activate(super.getClass());
      manager.getContext(TestContext.class).activate(this);
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helpers - Track events ----------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public static class EventRegisterObserver 
   {
      @Inject @ApplicationScoped
      private InstanceProducer<EventRegister> register;
      
      @Inject
      private Instance<SuiteContext> suiteContext;

      @Inject
      private Instance<ClassContext> classContext;

      @Inject
      private Instance<TestContext> testContext;
      
      @Inject
      private Instance<ContainerContext> containerContext;

      @Inject
      private Instance<DeploymentContext> deploymentContext;

      public void register(@Observes Object event)
      {
         if(register.get() == null)
         {
            register.set(new EventRegister());
         }
         
         EventRegister reg = register.get();
         
         reg.add(
               event.getClass(),
               new EventRecording()
                  .add(SuiteContext.class, suiteContext.get().isActive())
                  .add(ClassContext.class, classContext.get().isActive())
                  .add(TestContext.class, testContext.get().isActive())
                  .add(ContainerContext.class, containerContext.get().isActive())
                  .add(DeploymentContext.class, deploymentContext.get().isActive()));

         if(event instanceof Throwable)
         {
            // we are listening to Object which is not really a good thing, so throw exceptions if found.
            UncheckedThrow.throwUnchecked((Throwable)event);
         }
      }
   }
 
   public static class EventRegister 
   {
      private Map<Class<?>, List<EventRecording>> events;
      
      public EventRegister()
      {
         events = new HashMap<Class<?>, List<EventRecording>>();
      }
      
      public void add(Class<?> type, EventRecording recording)
      {
         if(events.get(type) == null)
         {
            List<EventRecording> recordings = new ArrayList<EventRecording>();
            recordings.add(recording);
            events.put(type, recordings);
         }
         else
         {
            events.get(type).add(recording);
         }
      }
      
      /**
       * Get the count of a assignable count.
       * 
       * @param type The assignable event type
       * @return Number of times fired
       */
      public Integer getCountTyped(Class<?> type)
      {
         int count = 0;
         for(Map.Entry<Class<?>, List<EventRecording>> recordingEntry : events.entrySet())
         {
            if(type.isAssignableFrom(recordingEntry.getKey())) 
            {
               count += recordingEntry.getValue().size();
            }
         }
         return count;
      }
      
      /**
       * Get the count of a specific type.
       * 
       * @param type The exact event type
       * @return Number of times fired
       */
      public Integer getCount(Class<?> type)
      {
         return events.containsKey(type) ? events.get(type).size():0;
      }
      
      public Boolean wasActive(Class<?> type, Class<? extends Context> context)
      {
         if(getCount(type) == 0)
         {
            return false;
         }
         for(EventRecording recording : events.get(type))
         {
            if(!recording.wasActive(context))
            {
               return false;
            }
         }
         return true;
      }

   }

   private static class EventRecording 
   {
      private Map<Class<? extends Context>, Boolean> activeContexts;
      
      public EventRecording()
      {
         activeContexts = new HashMap<Class<? extends Context>, Boolean>();
      }
      
      public EventRecording add(Class<? extends Context> context, Boolean isActive)
      {
         activeContexts.put(context, isActive);
         return this;
      }
      
      public Boolean wasActive(Class<? extends Context> context)
      {
         if(activeContexts.get(context) != null)
         {
            return activeContexts.get(context);
         }
         return false;
      }
   }
}