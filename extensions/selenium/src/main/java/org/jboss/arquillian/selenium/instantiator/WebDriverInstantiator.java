package org.jboss.arquillian.selenium.instantiator;

import org.jboss.arquillian.selenium.meta.ArquillianConfiguration;
import org.jboss.arquillian.selenium.meta.Configuration;
import org.jboss.arquillian.selenium.meta.OverridableConfiguration;
import org.jboss.arquillian.selenium.meta.SystemPropertiesConfiguration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Instantiates Selenium WebDriver implementation. Allows user to specify driver
 * class which be used during testing either by Arquillian configuration or a
 * system property.
 * 
 * Default implementation is HtmlUnitDriver. This instantiator is able to
 * instantiate even legacy WebDriver drivers.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @see HtmlUnitDriver
 * @see WebDriver
 * @see WebDriverInstantiator#IMPLEMENTATION_KEY
 */
public class WebDriverInstantiator implements Instantiator<WebDriver>
{
   /**
    * The System property key which holds class implementing WebDriver interface
    */
   public static final String IMPLEMENTATION_KEY = "arquillian.selenium.webdriver.implementation";

   // default implementation
   private static final String IMPLEMENTATION_DEFAULT = "org.openqa.selenium.htmlunit.HtmlUnitDriver";

   // configuration object
   private Configuration configuration;

   public WebDriverInstantiator()
   {
      this.configuration = new OverridableConfiguration(new ArquillianConfiguration(), new SystemPropertiesConfiguration());
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.selenium.instantiator.Instantiator#create()
    */
   public WebDriver create()
   {
      String implementation = configuration.getString(IMPLEMENTATION_KEY, IMPLEMENTATION_DEFAULT);

      WebDriver driver = SecurityActions.newInstance(implementation, new Class<?>[0], new Object[0], WebDriver.class);

      return driver;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.instantiator.Instantiator#destroy(java.lang
    * .Object)
    */
   public void destroy(WebDriver instance)
   {
      instance.quit();
   }

}
