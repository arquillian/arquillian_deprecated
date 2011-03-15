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

import java.lang.annotation.Annotation;

import org.jboss.arquillian.performance.annotation.Performance;
import org.jboss.arquillian.performance.exception.PerformanceException;
import org.jboss.arquillian.performance.meta.PerformanceMethodResult;
import org.jboss.arquillian.performance.meta.PerformanceSuiteResult;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.TestResult.Status;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.Test;

/**
 * 
 * TestPerformanceVerifier.
 * Verify that the test method execution time isnt longer that specified in the
 * Performance annotation.
 * @see org.jboss.arquillian.performance.annotation.Performance
 * 
 * @author <a href="mailto:stale.pedersen@jboss.org">Stale W. Pedersen</a>
 * @version $Revision: 1.1 $
 */
public class TestPerformanceVerifier 
{
   @Inject
   private Instance<TestResult> testResultInst;

   @Inject
   private Instance<PerformanceSuiteResult> suiteResultInst;
   
   public void callback(@Observes Test event) throws Exception
   {
      TestResult result = testResultInst.get();
      if(result != null)
      {
         //check if we have set a threshold
         Performance performance = null;
         Annotation[] annotations =  event.getTestMethod().getDeclaredAnnotations();
         for(Annotation a : annotations)
            if(a.annotationType().getName().equals(Performance.class.getCanonicalName()))
               performance = (Performance) a;
        
         if(performance != null)
         {
            if(performance.time() > 0 &&
               performance.time() < (result.getEnd()-result.getStart()))
            {
               result.setStatus(Status.FAILED);
               result.setThrowable(
                     new PerformanceException("The test didnt finish within the specified time: "
                           +performance.time()+"ms, it took "+(result.getEnd()-result.getStart())+"ms."));
            }
            
            // fetch suiteResult, get the correct classResult and append the test to that
            // classResult.
            PerformanceSuiteResult suiteResult = suiteResultInst.get();
            if(suiteResult != null)
               suiteResult.getResult(event.getTestClass().getName()).addMethodResult(
                     new PerformanceMethodResult(
                           performance.time(), 
                           (result.getEnd()-result.getStart()), 
                           event.getTestMethod()));
            else
               System.out.println("PerformanceVerifier didnt get PerformanceSuiteResult!");
         }
      }
   }
}
