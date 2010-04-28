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
package org.jboss.arquillian.prototyping.context.impl.openejb;

import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.prototyping.context.impl.BaseContextualResolver;
import org.jboss.arquillian.prototyping.context.spi.ContextualResolver;

/**
 * {@link ContextualResolver} implementation which can resolve/create 
 * JNDI {@link Context}s for the OpenEJB Container.  This resolver will
 * default the property {@link Context#INITIAL_CONTEXT_FACTORY}, then apply all
 * supplied user contextual properties to create and return a new {@link InitialContext}
 * instance.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class OpenEJBJndiContextResolver extends BaseContextualResolver implements ContextualResolver
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Singleton instance
    */
   private static final OpenEJBJndiContextResolver INSTANCE = new OpenEJBJndiContextResolver();

   /**
    * Value for {@link Context#INITIAL_CONTEXT_FACTORY} for OpenEJB
    */
   private static final String PROP_VALUE_OPENEJB_INITIAL_CONTEXT_FACTORY = "org.apache.openejb.client.LocalInitialContextFactory";

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Internal constructor; use instead {@link OpenEJBJndiContextResolver#getInstance()}
    */
   private OpenEJBJndiContextResolver()
   {

   }

   //-------------------------------------------------------------------------------------||
   // Factory ----------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains the single instance
    */
   public static OpenEJBJndiContextResolver getInstance()
   {
      return INSTANCE;
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

      // JNDI Context
      if (Context.class.isAssignableFrom(type))
      {
         return type.cast(createJndiContext(properties));
      }

      // None found
      return null;
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates a JNDI Naming context using the specified properties.  Will automatically
    * specify {@link Context#INITIAL_CONTEXT_FACTORY} if not explicitly 
    * supplied in properties
    */
   private Context createJndiContext(final Map<String, Object> properties)
   {
      // Create JNDI Context props
      final Properties propsUsedInContextCreation = new Properties();

      // Default the initial context property
      propsUsedInContextCreation.put(Context.INITIAL_CONTEXT_FACTORY, PROP_VALUE_OPENEJB_INITIAL_CONTEXT_FACTORY);

      // Load in the user-defined properties
      propsUsedInContextCreation.putAll(properties);

      // Create a new JNDI Context
      try
      {
         return new InitialContext(propsUsedInContextCreation);
      }
      catch (final NamingException e)
      {
         throw new RuntimeException("Could not create new JNDI Context", e);
      }

   }
}
