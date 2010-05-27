package org.jboss.arquillian.performance.event;

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
   
   
   public void callback(Context context, SuiteEvent event) throws Exception
   {
      PerformanceSuiteResult suiteResult = (PerformanceSuiteResult) context.get(PerformanceSuiteResult.class);
   
      if(suiteResult != null)
      {
         //TODO: compare, fetch, save.
         System.out.println("SuiteResult is ON!!!");
      }
   }

}
