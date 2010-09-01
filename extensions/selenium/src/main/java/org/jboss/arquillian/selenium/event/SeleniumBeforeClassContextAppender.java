package org.jboss.arquillian.selenium.event;

import org.jboss.arquillian.spi.ClassContextAppender;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.BeforeClass;

/**
 * Creates and bind Selenium WebDriver before each class of the test suite.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class SeleniumBeforeClassContextAppender implements ClassContextAppender
{

   /*
    * (non-Javadoc)
    * @see org.jboss.arquillian.spi.ClassContextAppender#append(org.jboss.arquillian.spi.Context)
    */
   public void append(Context context)
   {
      context.register(BeforeClass.class, new SeleniumStartupHandler());
   }
}
