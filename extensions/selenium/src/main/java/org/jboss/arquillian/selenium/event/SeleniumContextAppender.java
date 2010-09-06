package org.jboss.arquillian.selenium.event;

import org.jboss.arquillian.spi.ClassContextAppender;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.AfterClass;
import org.jboss.arquillian.spi.event.suite.Before;
import org.jboss.arquillian.spi.event.suite.BeforeClass;

/**
 * A class context appender responsible for fetching Selenium browser
 * configuration from Arquillian configuration, creating its instance and
 * injecting it before each method of the test is run. After all methods in 
 * test class are run, it safely destroys Selenium browser.
 * 
 * 
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see BeforeClass
 * @see Before
 * 
 */
public class SeleniumContextAppender implements ClassContextAppender
{
   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.spi.ClassContextAppender#append(org.jboss.arquillian
    * .spi.Context)
    */
   public void append(Context context)
   {
      context.register(AfterClass.class, new SeleniumShutdownHandler());
      context.register(BeforeClass.class, new SeleniumStartupHandler());
      context.register(Before.class, new SeleniumRetrievalHandler());
   }
}
