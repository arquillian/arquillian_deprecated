/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.selenium.instantiator;

import org.jboss.arquillian.selenium.SeleniumExtensionConfiguration;
import org.jboss.arquillian.selenium.spi.Instantiator;
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

   public WebDriverInstantiator()
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.selenium.instantiator.Instantiator#getPrecedence()
    */
   public int getPrecedence()
   {
      return 0;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.selenium.instantiator.Instantiator#create()
    */
   public WebDriver create(SeleniumExtensionConfiguration configuration)
   {
      WebDriver driver = SecurityActions.newInstance(configuration.getWebdriverImplementation(), new Class<?>[0], new Object[0], WebDriver.class);

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
