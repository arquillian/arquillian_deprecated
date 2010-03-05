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

import java.lang.reflect.Field;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

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
   protected InitialContext createContext() throws Exception
   {
      final Properties properties = new Properties();
      properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
      return new InitialContext(properties);
   }

   @Override
   protected Object lookupEJB(Field field) throws Exception 
   {
      InitialContext context = createContext();
      return lookupRecursive(field, context, context.listBindings("/"));
   }
   
   protected Object lookupRecursive(Field field, Context context, NamingEnumeration<Binding> contextNames) throws Exception 
   {
      while(contextNames.hasMore())
      {
         Binding contextName = contextNames.nextElement();
         Object value = contextName.getObject();
         if(Context.class.isInstance(value)) 
         {
            Context subContext = (Context)value;
            return lookupRecursive(field, subContext, subContext.listBindings("/"));
         }
         else 
         {
            value = context.lookup(contextName.getName());
            if(field.getType().isInstance(value))
            {
               return value;
            }
         }
      }
      throw new RuntimeException("Could not lookup EJB reference for: " + field);
   }
}
