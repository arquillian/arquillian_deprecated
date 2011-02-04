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
package org.jboss.arquillian.container.openwebbeans.embedded_1;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;

import org.apache.webbeans.spi.ContainerLifecycle;
import org.apache.webbeans.spi.ContextsService;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.container.AfterDeploy;
import org.jboss.arquillian.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.spi.event.suite.After;
import org.jboss.arquillian.spi.event.suite.Before;

/**
 * SessionLifecycle
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class LifecycleScopeHandler
{
   @Inject 
   private Instance<ContainerLifecycle> lifecycle;
   
   public void createSession(@Observes AfterDeploy event)
   {
      ContextsService service = this.lifecycle.get().getContextService();

      //service.startContext(ApplicationScoped.class, null);
      service.startContext(SessionScoped.class, null);
   }
   
   public void createRequest(@Observes Before event)
   {
      ContextsService service = this.lifecycle.get().getContextService();

      service.startContext(RequestScoped.class, null);
      service.startContext(ConversationScoped.class, null);
   }
   
   public void destroyRequest(@Observes After event)
   {
      ContextsService service = this.lifecycle.get().getContextService();

      service.endContext(ConversationScoped.class, null);
      service.endContext(RequestScoped.class, null);
   }

   public void destroySession(@Observes BeforeUnDeploy event)
   {
      ContextsService service = this.lifecycle.get().getContextService();

      //service.endContext(ApplicationScoped.class, null);
      service.endContext(SessionScoped.class, null);
   }
   
}
