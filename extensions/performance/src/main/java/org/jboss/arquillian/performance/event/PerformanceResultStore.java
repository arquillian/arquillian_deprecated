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

import org.jboss.arquillian.performance.meta.PerformanceClassResult;
import org.jboss.arquillian.performance.meta.PerformanceSuiteResult;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.SuiteEvent;

/**
 * 
 * 
 * 
 * fired during afterSuite
 * 
 * @author <a href="mailto:stale.pedersen@jboss.org">Stale W. Pedersen</a>
 * @version $Revision: 1.1 $
 */

public class PerformanceResultStore implements EventHandler<SuiteEvent>
{
   private final String folder = "arq-perf";
   private final SimpleDateFormat fileFormat = new SimpleDateFormat("dd.MM.yy.mm.ss");
   
   public void callback(Context context, SuiteEvent event) throws Exception
   {
      PerformanceSuiteResult suiteResult = (PerformanceSuiteResult) context.get(PerformanceSuiteResult.class);
         
      if(suiteResult != null)
      {
         //TODO: compare, fetch, save.
         System.out.println("SuiteResult is ON!!!");
         comparePerformanceSuiteResults(suiteResult);
      }
      else
         System.out.println("SUITERESULT IS NULL");
   }
   
   private void comparePerformanceSuiteResults(PerformanceSuiteResult suiteResult)
   {
      List<PerformanceSuiteResult> prevResults = findEarlierResults(suiteResult);
      
      for(PerformanceSuiteResult result : prevResults)
      {
         if(!doCompareResults(result, suiteResult))
            //throw some exception
            System.out.println("the new result wasnt within the specified delta");
         else
            System.out.println("the new result is OK");
      }
      
      //everything went well, now we just store the new result and we're done
      storePerformanceSuiteResult(suiteResult);
   }
   
   private boolean doCompareResults(PerformanceSuiteResult oldResult, PerformanceSuiteResult newResult)
   {
      for(String className : oldResult.getResults().keySet())
      {
         PerformanceClassResult oldClassResult = oldResult.getResult(className);
         PerformanceClassResult newClassResult = newResult.getResult(className);
         oldClassResult.compareResults(newClassResult);
      }
        // TODO Auto-generated method stub
      return true;
   }

   /**
    * 
    * @param suiteResult
    * @return
    */
   private List<PerformanceSuiteResult> findEarlierResults(final PerformanceSuiteResult currentResult)
   {
       File perfDir = new File(System.getProperty("user.dir")+File.separator+folder);
       File[] files = perfDir.listFiles(new FileFilter() {

         public boolean accept(File pathname)
         {
            if(pathname.getName().startsWith(currentResult.getName()))
               return true;
            else
               return false;
         }
          
       });
       List<PerformanceSuiteResult> prevResults = new ArrayList<PerformanceSuiteResult>();
       for(File f : files)
       {
          System.out.println("THESE ARE OUR PREV STORED TESTS: "+f.getName());
          PerformanceSuiteResult result = getResultFromFile(f);
          if(result != null)
             prevResults.add(result);
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
      catch(IOException ioe)
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
      String filename = suiteResult.getName()+"-"+fileFormat.format(new Date())+".ser";
      String currentPath = System.getProperty("user.dir")+File.separator+folder+File.separator;
      boolean filestatus = true;
      if(!new File(currentPath).isDirectory())
         filestatus = new File(currentPath).mkdirs();
      if(filestatus)
      {
         FileOutputStream fos = null;
         ObjectOutputStream out = null;
         try
         {
            fos = new FileOutputStream(currentPath+filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(suiteResult);
            out.close();
         }
         catch(IOException ex)
         {
            ex.printStackTrace();
         }
      }
   }

}
