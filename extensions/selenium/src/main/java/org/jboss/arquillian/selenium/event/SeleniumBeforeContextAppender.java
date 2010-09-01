package org.jboss.arquillian.selenium.event;

import org.jboss.arquillian.spi.ClassContextAppender;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.Before;

/**
 * Injects Selenium driver before execution of each test method
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class SeleniumBeforeContextAppender implements ClassContextAppender
{
   /*
    * (non-Javadoc)
    * @see org.jboss.arquillian.spi.ClassContextAppender#append(org.jboss.arquillian.spi.Context)
    */
   public void append(Context context)
   {
      context.register(Before.class, new SeleniumRetrievalHandler());
   }

}
