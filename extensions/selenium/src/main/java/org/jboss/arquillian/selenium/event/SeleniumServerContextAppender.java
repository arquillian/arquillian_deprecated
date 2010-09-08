package org.jboss.arquillian.selenium.event;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.SuiteContextAppender;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.jboss.arquillian.spi.event.suite.BeforeSuite;

/**
 * A suite context appender responsible for starting Selenium server. Selenium
 * server is started, if configured to do so, before the testsuite and
 * automatically killed after the suite run is finished.
 * 
 * 
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see AfterSuite
 * @see BeforeSuite
 * 
 */
public class SeleniumServerContextAppender implements SuiteContextAppender
{

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.spi.SuiteContextAppender#append(org.jboss.arquillian
    * .spi.Context)
    */
   public void append(Context context)
   {
      context.register(BeforeSuite.class, new SeleniumServerStartupHandler());
      context.register(AfterSuite.class, new SeleniumServerShutdownHandler());
   }

}
