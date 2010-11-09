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
package org.jboss.arquillian.container.openejb.embedded_3_1;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import javax.inject.Inject;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

import org.apache.openejb.assembler.classic.AppInfo;
import org.jboss.arquillian.prototyping.context.api.ArquillianContext;
import org.jboss.arquillian.prototyping.context.api.Properties;
import org.jboss.arquillian.prototyping.context.api.Property;
import org.jboss.arquillian.prototyping.context.impl.PropertiesImpl;
import org.jboss.arquillian.prototyping.context.impl.openejb.OpenEJBArquillianContextImpl;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.testenricher.ejb.EJBInjectionEnricher;

/**
 * {@link TestEnricher} implementation specific to the OpenEJB
 * Container
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class OpenEJBTestEnricher extends EJBInjectionEnricher
{
   @Inject
   private Instance<AppInfo> appInfo;
   
   private ArquillianContext arquillianContext = null;
   
   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.testenricher.ejb.EJBInjectionEnricher#enrich(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   @Override
   public void enrich(Object testCase)   {
      // Call the super implementation to handle @EJB
      super.enrich(testCase);

      // Handle Typesafe @Inject (ie. ask Arquillian for a an instance of the field type with no additional context properties)
      final Class<? extends Annotation> inject = (Class<? extends Annotation>) Inject.class;
      List<Field> fieldsWithInject = this.getFieldsWithAnnotation(testCase.getClass(), inject);
      for (final Field field : fieldsWithInject)
      {
         // Set accessible if it's not
         if (!field.isAccessible())
         {
            AccessController.doPrivileged(new PrivilegedAction<Void>()
            {

               public Void run()
               {
                  field.setAccessible(true);

                  // Return
                  return null;
               }
            });
         }
         try
         {
            /*
             *  Resolve (based on contextual properties if specified)
             */
            final Object resolvedVaue;
            final ArquillianContext arquillianContext = this.getArquillianContext();
            final Class<?> type = field.getType();

            // If Properties are defined
            if (field.isAnnotationPresent(Properties.class))
            {
               final Properties properties = field.getAnnotation(Properties.class);
               resolvedVaue = arquillianContext.get(type, properties);
            }
            // If just one property is defined
            else if (field.isAnnotationPresent(Property.class))
            {
               final Property property = field.getAnnotation(Property.class);
               final Properties properties = new PropertiesImpl(new Property[]
               {property});
               resolvedVaue = arquillianContext.get(type, properties);
            }
            // No properties defined; do type-based resolution only
            else
            {
               resolvedVaue = arquillianContext.get(type);
            }

            // Inject
            field.set(testCase, resolvedVaue);
         }
         catch (final IllegalAccessException e)
         {
            throw new RuntimeException("Could not inject into " + field.getName() + " of test case: " + testCase, e);
         }
      }

   }

   protected ArquillianContext getArquillianContext()
   {
      if (arquillianContext == null)
      {
         // Make a context
         final AppInfo deployment = appInfo.get();
         arquillianContext = new OpenEJBArquillianContextImpl(deployment);
      }
      return arquillianContext;
   }

   @Override
   protected InitialContext createContext() throws Exception
   {
      return this.getArquillianContext().get(InitialContext.class);
   }

   @Override
   protected Object lookupEJB(Class<?> fieldType) throws Exception
   {
      InitialContext initcontext = createContext();
      return lookupRecursive(fieldType, initcontext, initcontext.listBindings("/"));
   }

   //TODO No, no no: we must look up a known location from metadata, not search for a matching type in the whole JNDI tree
   protected Object lookupRecursive(Class<?> fieldType, javax.naming.Context context,
         NamingEnumeration<Binding> contextNames) throws Exception
   {
      while (contextNames.hasMore())
      {
         Binding contextName = contextNames.nextElement();
         Object value = contextName.getObject();
         if (javax.naming.Context.class.isInstance(value))
         {
            javax.naming.Context subContext = (javax.naming.Context) value;
            return lookupRecursive(fieldType, subContext, subContext.listBindings("/"));
         }
         else
         {
            value = context.lookup(contextName.getName());
            if (fieldType.isInstance(value))
            {
               return value;
            }
         }
      }
      throw new RuntimeException("Could not lookup EJB reference for: " + fieldType);
   }
}
