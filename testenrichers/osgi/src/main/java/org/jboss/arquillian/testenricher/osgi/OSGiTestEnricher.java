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
package org.jboss.arquillian.testenricher.osgi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.jboss.arquillian.osgi.OSGiContainer;
import org.jboss.arquillian.osgi.internal.EmbeddedOSGiContainer;
import org.jboss.arquillian.osgi.internal.RemoteOSGiContainer;
import org.jboss.arquillian.protocol.jmx.JMXMethodExecutor.ExecutionType;
import org.jboss.arquillian.protocol.jmx.JMXServerFactory;
import org.jboss.arquillian.protocol.jmx.JMXTestRunner;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.TestEnricher;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * The OSGi TestEnricher
 * 
 * The enricher supports the injection of the system BundleContext and the test Bundle.
 * 
 * <pre><code>
 *    @Inject 
 *    BundleContext context;
 * 
 *    @Inject
 *    Bundle bundle;
 * </code></pre>
 * 
 * @author thomas.diesler@jboss.com
 */
public class OSGiTestEnricher implements TestEnricher
{
   public void enrich(Context context, Object testCase)
   {
      Class<? extends Object> testClass = testCase.getClass();
      for (Field field : testClass.getDeclaredFields())
      {
         if (field.isAnnotationPresent(Inject.class))
         {
            if (field.getType().isAssignableFrom(OSGiContainer.class))
            {
               injectContainer(context, testCase, field);
            }
            else if (field.getType().isAssignableFrom(BundleContext.class))
            {
               injectBundleContext(context, testCase, field);
            }
            else if (field.getType().isAssignableFrom(Bundle.class))
            {
               injectBundle(context, testCase, field);
            }
         }
      }
   }

   public Object[] resolve(Context context, Method method)
   {
      return null;
   }

   private void injectContainer(Context context, Object testCase, Field field)
   {
      try
      {
         TestClass testClass = new TestClass(testCase.getClass());
         field.set(testCase, getContainer(context, testClass));
      }
      catch (IllegalAccessException ex)
      {
         throw new IllegalStateException("Cannot inject BundleContext", ex);
      }
   }

   private void injectBundleContext(Context context, Object testCase, Field field)
   {
      try
      {
         field.set(testCase, getBundleContext(context));
      }
      catch (IllegalAccessException ex)
      {
         throw new IllegalStateException("Cannot inject BundleContext", ex);
      }
   }

   private void injectBundle(Context context, Object testCase, Field field)
   {
      try
      {
         field.set(testCase, getTestBundle(context, testCase.getClass()));
      }
      catch (IllegalAccessException ex)
      {
         throw new IllegalStateException("Cannot inject Bundle", ex);
      }
   }

   private OSGiContainer getContainer(Context context, TestClass testClass)
   {
      BundleContext bundleContext = getBundleContext(context);

      if (JMXTestRunner.getExecutionType() == ExecutionType.REMOTE)
         return new RemoteOSGiContainer(bundleContext, testClass);
      else
         return new EmbeddedOSGiContainer(bundleContext, testClass);
   }

   private BundleContext getBundleContext(Context context)
   {
      BundleContext bundleContext = context.get(BundleContext.class);
      if (bundleContext == null)
         bundleContext = getBundleContextFromHolder();

      // Make sure this is really the system context
      bundleContext = bundleContext.getBundle(0).getBundleContext();
      return bundleContext;
   }

   private Bundle getTestBundle(Context context, Class<?> testClass)
   {
      Bundle testBundle = context.get(Bundle.class);
      if (testBundle == null)
      {
         // Get the test bundle from PackageAdmin with the test class as key 
         BundleContext bundleContext = getBundleContext(context);
         ServiceReference sref = bundleContext.getServiceReference(PackageAdmin.class.getName());
         PackageAdmin pa = (PackageAdmin)bundleContext.getService(sref);
         testBundle = pa.getBundle(testClass);
      }
      return testBundle;
   }

   private BundleContext getBundleContextFromHolder()
   {
      try
      {
         MBeanServer mbeanServer = JMXServerFactory.findOrCreateMBeanServer();
         ObjectName oname = new ObjectName(BundleContextHolder.OBJECT_NAME);
         if (mbeanServer.isRegistered(oname) == false)
            throw new IllegalStateException("BundleContextHolder not registered");

         BundleContextHolder holder = MBeanServerInvocationHandler.newProxyInstance(mbeanServer, oname, BundleContextHolder.class, false);
         return holder.getBundleContext();
      }
      catch (JMException ex)
      {
         throw new IllegalStateException("Cannot obtain system context", ex);
      }
   }
}
