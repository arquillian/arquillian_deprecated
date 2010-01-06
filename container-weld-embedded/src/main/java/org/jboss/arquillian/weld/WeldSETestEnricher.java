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
package org.jboss.arquillian.weld;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.weld.WeldSEContainer.WeldHolder;
import org.jboss.weld.manager.api.WeldManager;

/**
 * WeldSETestEnricher
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class WeldSETestEnricher implements TestEnricher
{
   @SuppressWarnings("unchecked")
   @Override
   public void enrich(Object testCase)
   {
      WeldHolder holder = WeldSEContainer.WELD_MANAGER.get();
      if (holder != null)
      {
         WeldManager manager = holder.getManager();
         CreationalContext<Object> creationalContext = manager.createCreationalContext(null);
         InjectionTarget<Object> injectionTarget = (InjectionTarget<Object>) manager
               .createInjectionTarget(
                     manager.createAnnotatedType(testCase.getClass()));
         
         injectionTarget.inject(testCase, creationalContext);
      }
   }
}
