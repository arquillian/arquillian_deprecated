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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.arquillian.performance.exception.PerformanceException;
import org.jboss.arquillian.performance.meta.PerformanceClassResult;
import org.jboss.arquillian.performance.meta.PerformanceSuiteResult;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.Test;

/**
 * Compares and stores test durations. 
 * 
 * fired during test
 * 
 * @author <a href="mailto:stale.pedersen@jboss.org">Stale W. Pedersen</a>
 * @version $Revision: 1.1 $
 */

public class PerformanceResultStore implements EventHandler<Test>
{
   private final String folder = "arq-perf";

   private final SimpleDateFormat fileFormat = new SimpleDateFormat("dd.MM.yy.mm.ss");

   public void callback(Context context, Test event) throws Exception
   {
      PerformanceSuiteResult suiteResult = (PerformanceSuiteResult) context.getParentContext().getParentContext()
            .get(PerformanceSuiteResult.class);

      if (suiteResult != null)
      {
         try
         {
            comparePerformanceSuiteResults(suiteResult, event.getTestMethod().getName());
         }
         catch (PerformanceException pe)
         {
            TestResult result = context.get(TestResult.class);
            if (result != null)
            {
               result.setThrowable(pe);
            }
         }
      }
   }

   private void comparePerformanceSuiteResults(PerformanceSuiteResult suiteResult, String testMethod)
         throws PerformanceException
   {
      List<PerformanceSuiteResult> prevResults = findEarlierResults(suiteResult);

      for (PerformanceSuiteResult result : prevResults)
      {
         doCompareResults(result, suiteResult, testMethod);
      }

      //everything went well, now we just store the new result and we're done
      storePerformanceSuiteResult(suiteResult);
   }

   private void doCompareResults(PerformanceSuiteResult oldResult, PerformanceSuiteResult newResult, String testMethod)
         throws PerformanceException
   {
      for (String className : oldResult.getResults().keySet())
      {

         PerformanceClassResult oldClassResult = oldResult.getResult(className);
         if (oldClassResult.getMethodResult(testMethod) != null)
         {
            oldClassResult.getMethodResult(testMethod).compareResults(
                  newResult.getResult(className).getMethodResult(testMethod),
                  oldClassResult.getPerformanceSpecs().resultsThreshold());
         }

      }

   }

   /**
    * 
    * @param suiteResult
    * @return
    */
   private List<PerformanceSuiteResult> findEarlierResults(final PerformanceSuiteResult currentResult)
   {
      File perfDir = new File(System.getProperty("user.dir") + File.separator + folder);
      File[] files = perfDir.listFiles(new FileFilter()
      {
         public boolean accept(File pathname)
         {
            if (pathname.getName().startsWith(currentResult.getName()))
               return true;
            else
               return false;
         }

      });
      List<PerformanceSuiteResult> prevResults = new ArrayList<PerformanceSuiteResult>();
      if (files != null)
      {
         for (File f : files)
         {
            //          System.out.println("THESE ARE OUR PREV STORED TESTS: "+f.getName());
            PerformanceSuiteResult result = getResultFromFile(f);
            if (result != null)
               prevResults.add(result);
         }
      }
      return prevResults;
   }

   private PerformanceSuiteResult getResultFromFile(File file)
   {
      try
      {
         FileInputStream fis = new FileInputStream(file);
         ObjectInputStream ois = new ObjectInputStream(fis);
         return (PerformanceSuiteResult) ois.readObject();
      }
      catch (IOException ioe)
      {
         return null;
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   /**
    * 1. make sure folder exists, if not create folder
    * 2. generate file name
    * 3. save file
    * 
    * @param suiteResult
    */
   private void storePerformanceSuiteResult(PerformanceSuiteResult suiteResult)
   {
      String filename = suiteResult.getName() + "-" + fileFormat.format(new Date()) + ".ser";
      String currentPath = System.getProperty("user.dir") + File.separator + folder + File.separator;
      boolean filestatus = true;
      if (!new File(currentPath).isDirectory())
         filestatus = new File(currentPath).mkdirs();
      if (filestatus)
      {
         FileOutputStream fos = null;
         ObjectOutputStream out = null;
         try
         {
            fos = new FileOutputStream(currentPath + filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(suiteResult);
            out.close();
         }
         catch (IOException ex)
         {
            System.err.println("Storing test results failed.");
            ex.printStackTrace();
         }
      }
   }

}
