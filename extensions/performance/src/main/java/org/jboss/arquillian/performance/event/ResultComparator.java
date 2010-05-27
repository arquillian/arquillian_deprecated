package org.jboss.arquillian.performance.event;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.SuiteEvent;

/**
 * 
 * 
 * 
 * fired in afterSuite.
 * 
 * @author <a href="mailto:stale.pedersen@jboss.org">Stale W. Pedersen</a>
 * @version $Revision: 1.1 $
 */
public class ResultComparator implements EventHandler<SuiteEvent>
{

   public void callback(Context context, SuiteEvent event) throws Exception
   {
      System.out.println("resultComparator");
   }
}
