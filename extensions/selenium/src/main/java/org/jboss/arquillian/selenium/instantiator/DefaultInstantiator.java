package org.jboss.arquillian.selenium.instantiator;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class DefaultInstantiator implements Instantiator<WebDriver>
{

   public WebDriver create()
   {
      return new HtmlUnitDriver();
   }

}
