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
package org.jboss.arquillian.prototyping.context.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.prototyping.context.api.ArquillianContext;
import org.jboss.arquillian.prototyping.context.api.Properties;
import org.jboss.arquillian.prototyping.context.api.Property;

/**
 * Base for implementations of {@link ArquillianContext}.
 * Provides container non-specific support. 
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public abstract class BaseContext implements ArquillianContext
{

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.prototyping.context.api.ArquillianContext#get(java.lang.Class)
    */
   @Override
   public <T> T get(final Class<T> type) throws IllegalArgumentException
   {
      final Map<String, Object> properties = Collections.emptyMap();
      return this.get(type, properties);
   }

   /**
    * @see org.jboss.arquillian.prototyping.context.api.ArquillianContext#get(java.lang.Class,org.jboss.arquillian.prototyping.context.api.Properties)
    */
   @Override
   public <T> T get(final Class<T> type, final Properties properties) throws IllegalArgumentException
   {
      // Precondition checks
      if (properties == null)
      {
         throw new IllegalArgumentException("properties must be specified");
      }
      // The class argument will be checked for null when delegated

      // Map properties to a java.util.Map
      final Map<String, Object> map = new HashMap<String, Object>();
      for (final Property property : properties.value())
      {
         map.put(property.key(), property.value());
      }

      // Delegate
      return this.get(type, map);
   }

}
