package org.jboss.arquillian.selenium.instantiator;

/**
 * SeleniumConstants encapsulates configuration constants shared between
 * Selenium client and Selenium server.
 * 
 * Selenium server configuration is used for legacy Selenium drivers.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @see SeleniumServerRunner
 * @see DefaultSeleniumInstantiator
 */
public final class SeleniumConstants
{
   /**
    * Port where Selenium server accepts requests
    */
   public static final String SERVER_PORT_KEY = "arquillian.selenium.server.port";

   /**
    * Time limit in milliseconds which determines operation failed, either for
    * executing Selenium command or starting Selenium server
    */
   public static final String TIMEOUT_KEY = "arquillian.selenium.timeout";

   static final int DEFAULT_SERVER_PORT = 14444;

   static final String DEFAULT_TIMEOUT = "60000";
}
