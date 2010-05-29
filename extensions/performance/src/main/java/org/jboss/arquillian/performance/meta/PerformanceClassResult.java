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
package org.jboss.arquillian.performance.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.performance.annotation.PerformanceTest;

/**
 * A PerformanceClassResult.
 * 
 * @author <a href="mailto:stale.pedersen@jboss.org">Stale W. Pedersen</a>
 * @version $Revision: 1.1 $
 */
public class PerformanceClassResult implements Serializable
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 6743015614324029428L;
   
   private List<PerformanceMethodResult> methodResults;
   private PerformanceTest performanceSpecs;
   private String testClassName;
   
   public PerformanceClassResult(PerformanceTest performanceSpecs, String testClassName)
   {
      setPerformanceSpecs(performanceSpecs);
      setTestClassName(testClassName);
      methodResults = new ArrayList<PerformanceMethodResult>();
   }

   public List<PerformanceMethodResult> getMethodResults()
   {
      return methodResults;
   }

   public void addMethodResult(PerformanceMethodResult methodResult)
   {
      methodResults.add(methodResult);
   }

   public PerformanceTest getPerformanceSpecs()
   {
      return performanceSpecs;
   }

   private void setPerformanceSpecs(PerformanceTest performanceSpecs)
   {
      this.performanceSpecs = performanceSpecs;
   }

   public String getTestClassName()
   {
      return testClassName;
   }

   private void setTestClassName(String testClassName)
   {
      this.testClassName = testClassName;
   }
   
   
}
