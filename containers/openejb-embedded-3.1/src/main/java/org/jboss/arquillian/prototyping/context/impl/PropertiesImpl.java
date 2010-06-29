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
import java.util.Map;

import org.jboss.arquillian.prototyping.context.api.Properties;
import org.jboss.arquillian.prototyping.context.api.Property;

/**
 * Value object metadata view by which {@link Properties}
 * may be represented.  Conceptually, a {@link Map} of key/value
 * pairs.  Immutable after construction.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@SuppressWarnings("all")
// Shh, we're gonna implement an annotation if we want to
public class PropertiesImpl implements Properties
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The properties
    */
   private final Property[] properties;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Constructs a new instance with the specified, required
    * properties
    * 
    * @param key
    * @param value
    * @throws IllegalArgumentException If the properties argument is null
    */
   public PropertiesImpl(final Property[] properties) throws IllegalArgumentException
   {
      // Precondition checks
      if (properties == null)
      {
         throw new IllegalArgumentException("properties must be specified");
      }

      // Defensive copy on set
      this.properties = this.copy(properties);
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.prototyping.context.api.Properties#value()
    */
   @Override
   public Property[] value()
   {
      // Return a copy so we can't be altered
      return this.copy(properties);
   }

   /**
    * {@inheritDoc}
    * @see java.lang.annotation.Annotation#annotationType()
    */
   @Override
   public Class<? extends Annotation> annotationType()
   {
      return Properties.class;
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Returns a copy of the specified array
    */
   private Property[] copy(final Property[] source)
   {
      assert source != null : "source must be specified";
      final int length = source.length;
      final Property[] copy = new Property[length];
      System.arraycopy(source, 0, copy, 0, length);
      return copy;
   }

}
