package org.jboss.arquillian.selenium.instantiator;

import org.jboss.arquillian.selenium.meta.ArquillianConfiguration;
import org.jboss.arquillian.selenium.meta.Configuration;
import org.jboss.arquillian.selenium.meta.OverridableConfiguration;
import org.jboss.arquillian.selenium.meta.SystemPropertiesConfiguration;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * Instantiator of the legacy Selenium driver. This driver requires Selenium
 * server to be running to connect to the browser.
 * 
 * By default, it launches Firefox driver and opens {@code
 * http://localhost:8080}. It expects Selenium server running on port {@code
 * 14444} on current machine.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class DefaultSeleniumInstantiator implements Instantiator<DefaultSelenium>
{
   /**
    * Host name of the machine where Selenium server is running
    */
   public static final String SERVER_HOST_KEY = "arquillian.selenium.server.host";

   /**
    * Port where Selenium server accepts requests
    */
   public static final String SERVER_PORT_KEY = "arquillian.selenium.server.port";

   /**
    * Identification of the browser for needs of Selenium.
    * 
    * Use can use variants including path to binary, such as: <i>*firefoxproxy
    * /opt/firefox-3.0/firefox</i>
    */
   public static final String BROWSER_KEY = "arquillian.selenium.browser";

   /**
    * The URL opened in the browser, which encapsulated the session
    */
   public static final String URL_KEY = "arquillian.selenium.url";

   /**
    * Time delay in milliseconds before each Selenium command is sent
    */
   public static final String SPEED_KEY = "arquillian.selenium.speed";

   /**
    * Time limit in milliseconds which determines operation failed
    */
   public static final String TIMEOUT_KEY = "arquillian.selenium.timeout";

   private static final String DEFAULT_SERVER_HOST = "localhost";
   private static final int DEFAULT_SERVER_PORT = 14444;
   private static final String DEFAULT_BROWSER = "*firefoxproxy";
   private static final String DEFAULT_URL = "http://localhost:8080";
   private static final String DEFAULT_SPEED = "0";
   private static final String DEFAULT_TIMEOUT = "60000";

   private Configuration configuration;

   public DefaultSeleniumInstantiator()
   {
      this.configuration = new OverridableConfiguration(new ArquillianConfiguration(), new SystemPropertiesConfiguration());
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.selenium.instantiator.Instantiator#create()
    */
   public DefaultSelenium create()
   {
      String server = configuration.getString(SERVER_HOST_KEY, DEFAULT_SERVER_HOST);
      int port = configuration.getInt(SERVER_PORT_KEY, DEFAULT_SERVER_PORT);
      String browser = configuration.getString(BROWSER_KEY, DEFAULT_BROWSER);
      String url = configuration.getString(URL_KEY, DEFAULT_URL);
      String speed = configuration.getString(SPEED_KEY, DEFAULT_SPEED);
      String timeout = configuration.getString(TIMEOUT_KEY, DEFAULT_TIMEOUT);

      DefaultSelenium selenium = new DefaultSelenium(server, port, browser, url);
      selenium.setSpeed(speed);
      selenium.setTimeout(timeout);
      selenium.start();

      return selenium;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.instantiator.Instantiator#destroy(java.lang
    * .Object)
    */
   public void destroy(DefaultSelenium instance)
   {
      instance.close();
      instance.stop();
   }

}
