/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.jboss;

import java.lang.reflect.Field;

import javax.ejb.EJB;
import javax.naming.InitialContext;

import org.jboss.arquillian.spi.TestEnricher;

/**
 * InjectionEnricher
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class InjectionEnricher implements TestEnricher
{
   private static final long serialVersionUID = 1L;

   @Override
   public void enrich(Object testCase)
   {
      injectClass(testCase);
   }

   void injectClass(Object testCase) 
   {
      try 
      {
         for(Field field : testCase.getClass().getDeclaredFields()) 
         {
            if(field.isAnnotationPresent(EJB.class)) 
            {
               Object ejb = lookupEJB(field);
               field.setAccessible(true);
               field.set(testCase, ejb);
            }
         }
      } 
      catch (Exception e) 
      {
         throw new RuntimeException("Could not inject members", e);
      }
   }

   private Object lookupEJB(Field field) throws Exception 
   {
      // TODO: figure out test context ? 
      InitialContext context = new InitialContext();
      return context.lookup("test/" + field.getType().getSimpleName() + "Bean/local");
   }
}
