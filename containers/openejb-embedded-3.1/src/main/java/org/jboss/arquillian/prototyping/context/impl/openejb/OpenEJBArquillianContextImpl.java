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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.openejb.assembler.classic.AppInfo;
import org.jboss.arquillian.prototyping.context.api.ArquillianContext;
import org.jboss.arquillian.prototyping.context.api.openejb.OpenEJBArquillianContext;
import org.jboss.arquillian.prototyping.context.impl.ArquillianContextResolver;
import org.jboss.arquillian.prototyping.context.impl.BaseContext;
import org.jboss.arquillian.prototyping.context.spi.ContextualResolver;

/**
 * OpenEJB Container implementation of a {@link ArquillianContext}. 
 * TODO Explain more.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class OpenEJBArquillianContextImpl extends BaseContext implements OpenEJBArquillianContext
{

   /*
    * TODO: Much of this logic is generic and should go into a base ChainedDelegatingContext
    * which consults a resolver chain (first match should be OK).  Only the definition of 
    * which resolvers in the chain are container-specific, and this is how we achieve composition
    */

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(OpenEJBArquillianContextImpl.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Delegate resolver chain
    */
   private final List<ContextualResolver> resolvers;

   /**
    * OpenEJB Metadata Deployment View 
    */
   private final AppInfo deployment;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates a new instance, setting all delegate resolvers
    * @param deployment The OpenEJB metadata representing the deployment
    * @throws IllegalArgumentException If the deployment is not specified
    */
   public OpenEJBArquillianContextImpl(final AppInfo deployment) throws IllegalArgumentException
   {
      // Precondition checks
      if (deployment == null)
      {
         throw new IllegalArgumentException("deployment must be specified");
      }

      // Create resolvers
      final List<ContextualResolver> resolvers = new ArrayList<ContextualResolver>();

      // Add resolvers
      resolvers.add(new ArquillianContextResolver(this));
      resolvers.add(OpenEJBJndiContextResolver.getInstance());

      // Log
      if (log.isLoggable(Level.FINE))
      {
         log.fine("Using resolvers: " + resolvers);
      }

      // Set
      this.resolvers = Collections.unmodifiableList(resolvers);
      this.deployment = deployment;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.prototyping.context.api.ArquillianContext#get(java.lang.Class, java.util.Map)
    */
   @Override
   public <T> T get(final Class<T> type, final Map<String, Object> properties) throws IllegalArgumentException
   {
      // Precondition checks
      if (type == null)
      {
         throw new IllegalArgumentException("type must be specified");
      }
      if (properties == null)
      {
         throw new IllegalArgumentException("properties must be specified");
      }

      // Delegate to the chain
      for (final ContextualResolver resolver : resolvers)
      {
         // Attempt to find a match from the delegate
         final T resolved = resolver.resolve(type, properties);
         // If we've found one, return
         if (resolved != null)
         {
            return resolved;
         }
      }

      // No conditions met
      return null;
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.prototyping.context.api.openejb.OpenEJBArquillianContext#getDeploymentMetadata()
    */
   @Override
   public AppInfo getDeploymentMetadata()
   {
      return deployment;
   }
}
