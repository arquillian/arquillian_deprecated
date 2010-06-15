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
package org.jboss.arquillian.weldee;

import java.util.UUID;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.Event;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.manager.api.WeldManager;

/**
 * SessionLifeCycleController
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class SessionLifeCycleController implements EventHandler<Event>
{
   private Class<? extends Event> endSessionEvent;
   
   public SessionLifeCycleController(Class<? extends Event> endSessionEvent) 
   {
      if(endSessionEvent == null) 
      {
         throw new IllegalArgumentException("EndSessionEvent must be specified");
      }
      this.endSessionEvent = endSessionEvent;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.EventHandler#callback(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, Event event) throws Exception 
   {
      WeldManager manager = context.get(WeldManager.class);
      if(manager == null)
      {
         throw new IllegalStateException("No " + WeldManager.class.getName() + " found in context");
      }
      ContextLifecycle lifeCycle = manager.getServices().get(ContextLifecycle.class);

      String sessionId = UUID.randomUUID().toString();
      BeanStore beanStore = new ConcurrentHashMapBeanStore();
      
      lifeCycle.restoreSession(sessionId, beanStore);
      context.register(endSessionEvent, new DestorySession(sessionId, beanStore));
   }
   
   /**
    * DestorySession
    *
    * @author <a href="mailto:aknutsen@redhat.org">Aslak Knutsen</a>
    * @version $Revision: $
    */
   private static class DestorySession implements EventHandler<Event> {
      
      private String sessionId;
      private BeanStore beanStore;
      
      public DestorySession(String sessionId, BeanStore beanStore)
      {
         this.sessionId = sessionId;
         this.beanStore = beanStore;
      }
      
      /* (non-Javadoc)
       * @see org.jboss.arquillian.spi.event.EventHandler#callback(org.jboss.arquillian.spi.Context, java.lang.Object)
       */
      public void callback(Context context, Event event) throws Exception
      {
         WeldManager manager = context.get(WeldManager.class);
         manager.getServices().get(ContextLifecycle.class).endSession(sessionId, beanStore);
      }
   }
}
