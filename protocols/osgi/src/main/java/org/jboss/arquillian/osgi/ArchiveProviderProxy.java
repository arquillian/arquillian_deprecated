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
package org.jboss.arquillian.osgi;

import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.arquillian.protocol.jmx.JMXServerFactory;

/**
 * A collection of OSGi container helper menthods that can be used by Arquillian tests.
 *
 * @author thomas.diesler@jboss.com
 * @since 06-Sep-2010
 */
public final class ArchiveProviderProxy 
{
   public static ObjectName createObjectName(String name)
   {
      try
      {
         return new ObjectName(name);
      }
      catch (MalformedObjectNameException e)
      {
         throw new Error("Invalid ObjectName: " + name + "; " + e);
      }
   }
   
   public static <T extends ArchiveProvider> T getProxy(Class<T> clazz)
   {
      ObjectName oname;
      try
      {
         oname = clazz.newInstance().getObjectName();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Cannot ObjectName for ArchiveProvider", ex);
      }
      return getMBeanProxy(oname, clazz);
   }

   @SuppressWarnings("unchecked")
   private static <T> T getMBeanProxy(ObjectName oname, Class<T> clazz)
   {
      Class<T> interf = null; 
      for (Class<?> aux : clazz.getInterfaces())
      {
         if (ArchiveProvider.class.isAssignableFrom(aux))
         {
            interf = (Class<T>)aux;
            break;
         }
      }
      if (interf == null)
         throw new IllegalArgumentException("Cannot find ArchiveProvider interface on: " + clazz);
      
      MBeanServer mbeanServer = JMXServerFactory.findOrCreateMBeanServer();
      return (T)MBeanServerInvocationHandler.newProxyInstance(mbeanServer, oname, interf, false);
   }
}
