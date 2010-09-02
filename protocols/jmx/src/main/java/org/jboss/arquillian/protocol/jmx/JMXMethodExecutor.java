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
package org.jboss.arquillian.protocol.jmx;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;

/**
 * JMXMethodExecutor
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class JMXMethodExecutor implements ContainerMethodExecutor
{
   private MBeanServerConnection mbeanServer;
   private boolean embeddedExecution;
   
   public JMXMethodExecutor(MBeanServerConnection mbeanServer, boolean embeddedExecution)
   {
      if (mbeanServer == null)
         throw new IllegalArgumentException("Null mbeanServer");
      
      this.mbeanServer = mbeanServer;
      this.embeddedExecution = embeddedExecution;
   }

   public TestResult invoke(TestMethodExecutor testMethodExecutor)
   {
      if(testMethodExecutor == null) 
         throw new IllegalArgumentException("TestMethodExecutor null");
      
      String testClass = testMethodExecutor.getInstance().getClass().getName();
      String testMethod = testMethodExecutor.getMethod().getName();

      TestResult result = null;
      try 
      {
         ObjectName objectName = new ObjectName(JMXTestRunnerMBean.OBJECT_NAME);
         JMXTestRunnerMBean testRunner = getMBeanProxy(objectName, JMXTestRunnerMBean.class);

         if (embeddedExecution == true)
         {
            byte[] resultBytes = testRunner.runTestMethodSerialized(testClass, testMethod);
            ByteArrayInputStream resultStream = new ByteArrayInputStream(resultBytes);
            ObjectInputStream ois = new ObjectInputStream(resultStream);
            result = (TestResult)ois.readObject();
         }
         else
         {
            result = testRunner.runTestMethod(testClass, testMethod);
         }
      }
      catch (final Throwable e) 
      {
         result = new TestResult(Status.FAILED);
         result.setThrowable(e);
      }
      finally
      {
         result.setEnd(System.currentTimeMillis());
      }
      return result;
   }

   private <T> T getMBeanProxy(ObjectName name, Class<T> interf)
   {
      return (T)MBeanServerInvocationHandler.newProxyInstance(mbeanServer, name, interf, false);
   }
}