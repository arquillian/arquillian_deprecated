package org.jboss.arquillian.selenium.event;

import org.jboss.arquillian.selenium.instantiator.SeleniumServerRunner;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.SuiteEvent;

/**
 * A handler which starts Selenium server and binds it the current context. The
 * server instance is stored in {@link SeleniumServerRunner}.
 * 
 * The Selenium server run is <i>disabled</i> by default, it must be allowed
 * either in Arquillian configuration or by a system property.
 * 
 * <br/>
 * <b>Exports:</b><br/> {@link SeleniumServerRunner}</br> <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @see SeleniumServerRunner
 * @see SeleniumServerRunner#SERVER_ENABLE_KEY
 * 
 */
public class SeleniumServerStartupHandler implements EventHandler<SuiteEvent>
{

   /*
    * (non-Javadoc)
    * 
    * @seeorg.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.
    * arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, SuiteEvent event) throws Exception
   {
      SeleniumServerRunner server = new SeleniumServerRunner();
      server.start();

      context.add(SeleniumServerRunner.class, server);
   }

}
