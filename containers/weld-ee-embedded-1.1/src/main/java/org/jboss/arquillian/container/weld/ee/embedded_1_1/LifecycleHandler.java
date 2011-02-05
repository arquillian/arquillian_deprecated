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
package org.jboss.arquillian.container.weld.ee.embedded_1_1;

import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ClassScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.container.AfterDeploy;
import org.jboss.arquillian.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.spi.event.suite.After;
import org.jboss.arquillian.spi.event.suite.Before;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequest;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.context.bound.MutableBoundRequest;
import org.jboss.weld.manager.api.WeldManager;

/**
 * If No WeldManager found in context, ignore event. This can happen if deployment failure, or @Expected runs. <br/>
 * 
 * A Extension should handle not having the correct context.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class LifecycleHandler
{
   @Inject @ClassScoped 
   private InstanceProducer<CDISessionMap> sessionMap;
   
   @Inject @ClassScoped 
   private InstanceProducer<CDIRequestMap> requestMap;

   @Inject @ClassScoped
   private InstanceProducer<CDIConversationID> conversationId;
   
   @Inject @ClassScoped
   private InstanceProducer<BoundRequest> boundRequest;
   
   private boolean enableConversationScope = false;
   
   public void configure(@Observes WeldEEMockConfiguration configuration)
   {
      enableConversationScope = configuration.isEnableConversationScope();
   }
   
   public void createSession(@Observes AfterDeploy event, WeldManager manager)
   {
      BoundSessionContext sessionContext = manager.instance().select(BoundSessionContext.class).get();

      CDISessionMap map = new CDISessionMap();
      sessionContext.associate(map);
      sessionContext.activate();
      sessionMap.set(map);
   }

   public void destroySession(@Observes BeforeUnDeploy event, WeldManager manager)
   {
      BoundSessionContext sessionContext = manager.instance().select(BoundSessionContext.class).get();

      CDISessionMap map = sessionMap.get();
      if(map != null)
      {
         try
         {
            sessionContext.invalidate();
            sessionContext.deactivate();
         }
         finally
         {
            sessionContext.dissociate(map);
         }
      }
   }
   
   public void createRequest(@Observes Before event, WeldManager manager)
   {
      BoundRequestContext requestContext = manager.instance().select(BoundRequestContext.class).get();
      
      CDIRequestMap map = new CDIRequestMap();
      requestContext.associate(map);
      requestContext.activate();
      requestMap.set(map);
   }

   public void destroyRequest(@Observes After event, WeldManager manager)
   {
      BoundRequestContext requestContext = manager.instance().select(BoundRequestContext.class).get();
      
      CDIRequestMap map = requestMap.get();
      if (map != null)
      {
         try
         {
            requestContext.invalidate();
            requestContext.deactivate();
         }
         finally
         {
            requestContext.dissociate(map);
            map.clear();
         }
      }
   }

   public void createConversation(@Observes(precedence = -1) Before event, WeldManager manager)
   {
      if(!enableConversationScope)
      {
         return;
      }

      CDIConversationID id = conversationId.get();
      if(id == null)
      {
         id = new CDIConversationID(null); // when null creates a new empty conversation id. 
      }
      
      BoundRequest request = new MutableBoundRequest(requestMap.get(), sessionMap.get());
      this.boundRequest.set(request);
      
      BoundConversationContext conversationContext = manager.instance().select(BoundConversationContext.class).get();
      conversationContext.associate(request);
      conversationContext.activate(id.getId());
   }

   public void destroyConversation(@Observes(precedence = 1) After event, WeldManager manager)
   {
      if(!enableConversationScope)
      {
         return;
      }

      BoundConversationContext conversationContext = manager.instance().select(BoundConversationContext.class).get();
      if (!conversationContext.getCurrentConversation().isTransient())
      {
         conversationId.set(new CDIConversationID(conversationContext.getCurrentConversation().getId()));
      }
      else
      {
         conversationId.set(new CDIConversationID(null));
      }

      BoundRequest request = boundRequest.get();

      try
      {
         conversationContext.invalidate();
         conversationContext.deactivate();
      }
      finally
      {
         conversationContext.dissociate(request);
      }
   }
}
