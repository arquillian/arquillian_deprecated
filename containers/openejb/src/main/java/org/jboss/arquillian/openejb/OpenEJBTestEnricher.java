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
package org.jboss.arquillian.openejb;

import java.util.Properties;


import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestEnricher;
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

   @Override
   protected InitialContext createContext(Context context) throws Exception
   {
      final Properties properties = new Properties();
      properties.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
      return new InitialContext(properties);
   }

   @Override
   protected Object lookupEJB(Context context, Class<?> fieldType) throws Exception
   {
      InitialContext initcontext = createContext(context);
      return lookupRecursive(fieldType, initcontext, initcontext.listBindings("/"));
   }
   
   protected Object lookupRecursive(Class<?> fieldType, javax.naming.Context context, NamingEnumeration<Binding> contextNames) throws Exception 
   {
      while(contextNames.hasMore())
      {
         Binding contextName = contextNames.nextElement();
         Object value = contextName.getObject();
         if(javax.naming.Context.class.isInstance(value)) 
         {
            javax.naming.Context subContext = (javax.naming.Context)value;
            return lookupRecursive(fieldType, subContext, subContext.listBindings("/"));
         }
         else 
         {
            value = context.lookup(contextName.getName());
            if(fieldType.isInstance(value))
            {
               return value;
            }
         }
      }
      throw new RuntimeException("Could not lookup EJB reference for: " + fieldType);
   }
}
