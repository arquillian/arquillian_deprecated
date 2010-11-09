/*
 * JBoss, Community-driven Open Source Middleware
 * Copyright 2010, JBoss by Red Hat, Inc., and individual contributors
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
package org.jboss.arquillian.container.openwebbeans.embedded_1;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.openwebbeans.embedded_1.OpenWebBeansSEContainer.ContainerInstanceHolder;
import org.jboss.arquillian.testenricher.cdi.CDIInjectionEnricher;

/**
 * A CDI injection enricher adaptor that retrieves the {@link BeanManager} from
 * the thread local for the current OpenWebBeans container instance.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @author <a href="mailto:dan.allen@mojavelinux.com">Dan Allen</a>
 * @version $Revision: $
 */
public class OpenWebBeansSETestEnricher extends CDIInjectionEnricher
{
   @Override
   protected BeanManager lookupBeanManager()
   {
      ContainerInstanceHolder holder = OpenWebBeansSEContainer.CONTAINER_INSTANCE_HOLDER.get();
      if (holder != null)
      {
         return holder.getManager();
      }
      return null;
   }
}
