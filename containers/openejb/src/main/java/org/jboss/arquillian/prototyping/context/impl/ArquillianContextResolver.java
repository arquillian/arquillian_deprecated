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

import java.util.Map;

import org.jboss.arquillian.prototyping.context.api.ArquillianContext;
import org.jboss.arquillian.prototyping.context.spi.ContextualResolver;

/**
 * {@link ContextualResolver} implementation which can resolve
 * to a supplied {@link ArquillianContext}
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ArquillianContextResolver extends BaseContextualResolver implements ContextualResolver
{
   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The instance to be returned if requesting an {@link ArquillianContext}
    */
   private final ArquillianContext delegate;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Constructs a new instance which is to resolve {@link ArquillianContext}s
    * to the specified instance
    * @param delegate The instance to be returned if {@link ArquillianContextResolver#get(Class, Map)}
    * encounters a request for {@link ArquillianContext} type
    * @throws IllegalArgumentException If the delegate is not specified
    */
   public ArquillianContextResolver(final ArquillianContext delegate) throws IllegalArgumentException
   {
      // Precondition checks
      if (delegate == null)
      {
         throw new IllegalArgumentException("delegate must be specified");
      }
      this.delegate = delegate;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.prototyping.context.spi.ContextualResolver#resolve(java.lang.Class, java.util.Map)
    */
   @Override
   public <T> T resolve(final Class<T> type, final Map<String, Object> properties) throws IllegalArgumentException
   {

      // Do we handle this?
      if (ArquillianContext.class.isAssignableFrom(type))
      {
         return type.cast(delegate);
      }

      // None found
      return null;
   }
}
