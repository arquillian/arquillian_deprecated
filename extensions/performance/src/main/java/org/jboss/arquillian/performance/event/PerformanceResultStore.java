package org.jboss.arquillian.performance.event;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
   private static final SimpleDateFormat fileFormat = new SimpleDateFormat("dd.MM.yy.mm.ss");
//   private final String filename = ""
   
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
      storePerformanceSuiteResult(suiteResult);
   }
   
   private List<PerformanceSuiteResult> findEarlierResults(PerformanceSuiteResult suiteResult)
   {
       
      return null;
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
