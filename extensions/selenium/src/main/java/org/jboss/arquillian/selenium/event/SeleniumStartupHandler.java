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
package org.jboss.arquillian.selenium.event;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.selenium.SeleniumExtensionConfiguration;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.instantiator.InstantiatorUtil;
import org.jboss.arquillian.selenium.spi.Instantiator;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * A handler which creates a Selenium browser, Selenium WebDriver or Cheiron
 * implementation and binds it to the current context. The instance is stored in {@link SeleniumHolder}, which defines default implementation if no
 * configuration is found in arquillian.xml file<br/>
 * <br/>
 * <b>Imports:</b><br/> {@link Selenium}<br/>
 * <br/> @{link {@link Configuration}<br/>
 * <br/>
 * <b>Exports:</b><br/> {@link SeleniumHolder}</br> <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see Selenium
 * @see SeleniumHolder
 * 
 */
public class SeleniumStartupHandler implements EventHandler<ClassEvent>
{
   private static final Logger log = Logger.getLogger(SeleniumStartupHandler.class.getName());

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.
    * arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, ClassEvent event) throws Exception
   {
      prepareContext(context, event.getTestClass());
   }

   private void prepareContext(Context context, TestClass testClass)
   {
      Configuration configuration = context.get(Configuration.class);
      
      SeleniumExtensionConfiguration seleniumConfiguration = configuration.getExtensionConfig(SeleniumExtensionConfiguration.class);
      
      SeleniumHolder holder = new SeleniumHolder();
      for (Field f : SecurityActions.getFieldsWithAnnotation(testClass.getJavaClass(), Selenium.class))
      {
         Class<?> typeClass = f.getType();
         if (holder.contains(typeClass))
            break;

         Instantiator<?> instantiator = InstantiatorUtil.highest(InstantiatorUtil.filter(context.getServiceLoader().all(Instantiator.class), typeClass));
         if (instantiator == null)
         {
            throw new IllegalArgumentException("No creation method was found for object of type " + typeClass.getName());
         }

         if (log.isLoggable(Level.FINE))
         {
            log.fine("Using instantiator defined in class: " + instantiator.getClass().getName() + ", with precedence " + instantiator.getPrecedence());
         }
         holder.hold(typeClass, typeClass.cast(instantiator.create(seleniumConfiguration)), instantiator);

      }
      context.add(SeleniumHolder.class, holder);
   }
}
