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
package org.jboss.arquillian.performance.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PerformanceTest defines a way of storing test results and compare them
 * with previously stored tests. If newer tests results are worse than a 
 * specified threshold an exception will be thrown.
 * 
 * It is possible to specify if you want to compare each test method or
 * the combined total time of all the test methods.
 * 
 * How threshold is calculated:
 * threshold * previous_result < latest result.
 * Note that threshold can not be lower than 1.
 * 
 * Test results are stored in the folder arq-tests on project root.
 * 
 * @author <a href="mailto:stale.pedersen@jboss.org">Stale W. Pedersen</a>
 * @version $Revision: 1.1 $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface PerformanceTest {

   /**
    * Set the threshold of comparing new and old results. Can not be lower than 1 (which is default). 
    * 
    * @return
    */
   double resultsThreshold() default 1d;
   /**
    * Will cause the performance check to merge previous results and only store the
    * best result.
    * 
    * @return
    */
   boolean mergeTestResults() default false;
}
