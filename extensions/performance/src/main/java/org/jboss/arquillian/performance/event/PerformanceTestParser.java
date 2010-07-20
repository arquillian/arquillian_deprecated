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
package org.jboss.arquillian.performance.event;

import org.jboss.arquillian.performance.annotation.*;
import org.jboss.arquillian.performance.meta.PerformanceClassResult;
import org.jboss.arquillian.performance.meta.PerformanceSuiteResult;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * 
 * A PerformanceRuleParser.
 * 
 * @author <a href="mailto:stale.pedersen@jboss.org">Stale W. Pedersen</a>
 * @version $Revision: 1.1 $
 */
public class PerformanceTestParser implements EventHandler<ClassEvent>
{
   /**
    * @see org.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, ClassEvent event) throws Exception
   {
      parsePerformanceRules(context, event.getTestClass());
   }
   
   public void parsePerformanceRules(Context context, TestClass testClass)
   {
      PerformanceTest performanceTest = (PerformanceTest) testClass.getAnnotation(PerformanceTest.class);
      if(performanceTest != null)
      {
         PerformanceClassResult classPerformance = 
            new PerformanceClassResult(performanceTest, testClass.getName());
         
         PerformanceSuiteResult suitePerformance = 
            context.getParentContext().get(PerformanceSuiteResult.class);
         if(suitePerformance == null)
         {
            suitePerformance = new PerformanceSuiteResult(classPerformance.getTestClassName());
            System.out.println("adding performancesuiteresult to context");
            context.getParentContext().add(PerformanceSuiteResult.class, suitePerformance);
         }
         
         suitePerformance.addClassResult(testClass.getName(), classPerformance);
//         setThreshold(performanceTest.resultsThreshold());
       System.out.println("PerformanceTest is: "+performanceTest.resultsThreshold());
      }
      else
         System.out.println("PerformanceTest is NULL!");
         
   }
}
