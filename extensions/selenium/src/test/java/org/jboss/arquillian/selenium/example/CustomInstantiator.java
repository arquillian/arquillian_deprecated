package org.jboss.arquillian.selenium.example;

import org.jboss.arquillian.selenium.instantiator.Instantiator;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * Instantiator of the legacy Selenium driver with hard-coded parameters
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 * @see DefaultSelenium
 */
public class CustomInstantiator implements Instantiator<DefaultSelenium>
{

   public DefaultSelenium create()
   {
      DefaultSelenium selenium = new DefaultSelenium("localhost", 14444, "*firefoxproxy", "http://localhost:8080");
      selenium.start();

      return selenium;
   }

   public void destroy(DefaultSelenium instance)
   {
      instance.close();
      instance.stop();
   }

}
