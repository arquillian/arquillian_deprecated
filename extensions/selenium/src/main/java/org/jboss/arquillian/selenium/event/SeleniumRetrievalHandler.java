package org.jboss.arquillian.selenium.event;

import java.lang.reflect.Field;
import java.util.List;

import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.TestEvent;
import org.openqa.selenium.WebDriver;

public class SeleniumRetrievalHandler implements EventHandler<TestEvent>
{

   public void callback(Context context, TestEvent event) throws Exception
   {
      injectSelenium(context, event.getTestClass().getJavaClass(), event.getTestInstance());
   }

   private void injectSelenium(Context context, Class<?> clazz, Object testInstance)
   {

      List<Field> fields = SecurityActions.getFieldsWithAnnotation(clazz, Selenium.class);
      WebDriver driver = context.get(SeleniumStartupHandler.SELENIUM);

      try
      {
         for (Field f : fields)
         {
            // omit setting if already set
            if (f.get(testInstance) == null)
            {
               f.set(testInstance, driver);
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not inject members", e);
      }

   }

}
