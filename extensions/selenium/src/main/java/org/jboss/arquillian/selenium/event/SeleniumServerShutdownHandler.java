package org.jboss.arquillian.selenium.event;

import org.jboss.arquillian.selenium.instantiator.SeleniumServerRunner;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.SuiteEvent;

/**
 * A handler which stops Selenium server. The instance is stored in
 * {@link SeleniumServerRunner} in suite context.
 * 
 * The Selenium server run is <i>disabled</i> by default, it must be allowed
 * either in Arquillian configuration or by a system property.
 * 
 * <br/>
 * <b>Imports:</b><br/> {@link SeleniumServerRunner}</br> <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @see SeleniumServerRunner
 * @see SeleniumServerRunner#SERVER_ENABLE_KEY
 * 
 */
public class SeleniumServerShutdownHandler implements EventHandler<SuiteEvent>
{

   /*
    * (non-Javadoc)
    * 
    * @seeorg.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.
    * arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, SuiteEvent event) throws Exception
   {
      SeleniumServerRunner server = context.get(SeleniumServerRunner.class);
      System.out.println("Shutdown server called");
      server.stop();
   }

}
