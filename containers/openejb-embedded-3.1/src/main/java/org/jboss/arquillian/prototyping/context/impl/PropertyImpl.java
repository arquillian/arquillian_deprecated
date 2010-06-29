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

import java.lang.annotation.Annotation;

import org.jboss.arquillian.prototyping.context.api.Property;

/**
 * Value object metadata view by which {@link Property}
 * may be represented.  A contextual key/value property
 * implementation.  Immutable after construction.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@SuppressWarnings("all")
// Shh, we're gonna implement an annotation if we want to
public class PropertyImpl implements Property
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The key
    */
   private final String key;

   /**
    * The value
    */
   private final String value;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Constructs a new instance with the specified, required
    * key and value
    * 
    * @param key
    * @param value
    * @throws IllegalArgumentException If either argument is null or empty
    */
   public PropertyImpl(final String key, final String value) throws IllegalArgumentException
   {
      // Precondition checks
      if (key == null || key.length() == 0)
      {
         throw new IllegalArgumentException("key must be specified and not empty");
      }
      if (value == null || value.length() == 0)
      {
         throw new IllegalArgumentException("value must be specified and not empty");
      }

      // Set
      this.key = key;
      this.value = value;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.prototyping.context.api.Property#key()
    */
   @Override
   public String key()
   {
      return key;
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.prototyping.context.api.Property#value()
    */
   @Override
   public String value()
   {
      return value;
   }

   /**
    * {@inheritDoc}
    * @see java.lang.annotation.Annotation#annotationType()
    */
   @Override
   public Class<? extends Annotation> annotationType()
   {
      return Property.class;
   }

}
