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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.resource.spi.ResourceAdapter;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.testenricher.resource.ResourceInjectionEnricher;

/**
 * {@link TestEnricher} implementation specific to the OpenEJB
 * Container for injecting <code>@Resource</code> annotated
 * fields and method parameters.
 * 
 * @author David Allen
 *
 */
public class OpenEJBResourceInjectionEnricher extends ResourceInjectionEnricher
{
   private static final String RESOURCE_ADAPTER_LOOKUP_PREFIX = "openejb/Resource";

   @Inject
   private Instance<Context> contextInstance;

   @Override
   protected Context getContainerContext() throws NamingException
   {
      return contextInstance.get();
   }

   @Override
   protected Object resolveResource(AnnotatedElement element) throws Exception
   {
      Object resolvedResource = null;
      Class<?> resourceType = null;
      if (Field.class.isAssignableFrom(element.getClass()))
      {
         resourceType = ((Field) element).getType();
      }
      else if (Method.class.isAssignableFrom(element.getClass()))
      {
         resourceType = ((Method) element).getParameterTypes()[0];
      }
      if (resourceType == null)
      {
         throw new IllegalStateException("No type found for resource injection target " + element);
      }

      // If the element type is a resource adapter, then apply special rules
      // for looking it up in JNDI
      if (ResourceAdapter.class.isAssignableFrom(resourceType)
            || DataSource.class.isAssignableFrom(resourceType))
      {
         Resource resourceAnnotation = element.getAnnotation(Resource.class);
         if (!resourceAnnotation.name().equals(""))
         {
            resolvedResource = lookup(RESOURCE_ADAPTER_LOOKUP_PREFIX + "/" + resourceAnnotation.name());
         }
         else if (!resourceAnnotation.mappedName().equals(""))
         {
            resolvedResource = lookup(resourceAnnotation.mappedName());
         }
         else
         {
            resolvedResource = findResourceByType(resourceType);
         }
      }
      else if (UserTransaction.class.isAssignableFrom(resourceType))
      {
         resolvedResource = lookup("java:comp/UserTransaction");
      }
      
      return resolvedResource;
   }

   private Object findResourceByType(Class<?> resourceType) throws NamingException
   {
      NamingEnumeration<Binding> namingEnumeration = null;
      try 
      {
         namingEnumeration = getContainerContext().listBindings(RESOURCE_ADAPTER_LOOKUP_PREFIX);
      }
      catch (NamingException ignore)
      {
         // No resource adapters exist, so we don't find anything here
      }
      List<Object> resourceMatches = new ArrayList<Object>();
      while ((namingEnumeration != null) && (namingEnumeration.hasMoreElements()))
      {
         Binding binding = namingEnumeration.next();
         Object boundResource = binding.getObject();
         if (resourceType.isAssignableFrom(boundResource.getClass()))
         {
            resourceMatches.add(boundResource);
         }
      }
      if (resourceMatches.size() == 1)
      {
         return resourceMatches.get(0);
      }
      else if (resourceMatches.size() > 1)
      {
         // Throw some ambiguous matches exception perhaps?
         return resourceMatches.get(0);
      }
      throw new RuntimeException("Could not inject resource of type " + resourceType);
   }

}
