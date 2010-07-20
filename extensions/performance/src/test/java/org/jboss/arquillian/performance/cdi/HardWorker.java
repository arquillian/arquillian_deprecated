package org.jboss.arquillian.performance.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@ApplicationScoped
public class HardWorker
{

   @Produces @Named @WorkHard double workingHard()
   {
      
      Exception e = new RuntimeException();
      System.out.println("stacktrace before sleeping");
      e.printStackTrace();
      try
      {
         long time = System.currentTimeMillis();
         System.out.println("current time before sleep: "+System.currentTimeMillis());
         Thread.currentThread().sleep(2000);
         System.out.println("slept for: "+(System.currentTimeMillis()-time));
         System.out.println("current time after sleep: "+System.currentTimeMillis());
      }
      catch (InterruptedException ie)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return 21;
   }
}
