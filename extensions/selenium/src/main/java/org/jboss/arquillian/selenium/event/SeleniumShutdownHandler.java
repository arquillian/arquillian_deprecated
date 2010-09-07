package org.jboss.arquillian.selenium.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.instantiator.Instantiator;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * A handler which destroys a Selenium browser, Selenium WebDriver or Cheiron
 * instance from the current context. <br/>
 * <br/>
 * <b>Imports:</b><br/> {@link Selenium}<br/>
 * 
 * @{link {@link SeleniumHolder}<br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see Selenium
 * @see SeleniumHolder
 * 
 */
public class SeleniumShutdownHandler implements EventHandler<ClassEvent>
{
   /*
    * (non-Javadoc)
    * 
    * @seeorg.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.
    * arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, ClassEvent event) throws Exception
   {
      clearContext(context, event.getTestClass());

   }

   private void clearContext(Context context, TestClass testClass)
   {
      SeleniumHolder holder = context.get(SeleniumHolder.class);

      for (Field f : SecurityActions.getFieldsWithAnnotation(testClass.getJavaClass(), Selenium.class))
      {
         Class<?> typeClass = f.getType();
         if (!holder.contains(typeClass))
            break;

         Selenium annotation = f.getAnnotation(Selenium.class);
         Class<?>[] instantiatorClasses = annotation.instantiator();
         destroySelenium(holder, instantiatorClasses, typeClass);
      }
   }

   private void destroySelenium(SeleniumHolder holder, Class<?>[] instantiatorClasses, Class<?> typeClass)
   {
      List<Method> destroyers = new ArrayList<Method>();
      for (Class<?> instantiatorClass : instantiatorClasses)
      {
         destroyers.addAll(SecurityActions.getMethodsWithSignature(instantiatorClass, "destroy", Void.TYPE, typeClass));
      }

      if (destroyers.size() == 0)
      {
         throw new RuntimeException("No destroyer method was found for object of type " + typeClass.getName());
      }
      if (destroyers.size() != 1)
      {
         throw new RuntimeException("Could not determine which destroyer method should be used to destroy object of type " + typeClass.getName() + " because there are multiple methods available.");
      }

      try
      {
         Instantiator<?> destroyer = SecurityActions.newInstance(destroyers.get(0).getDeclaringClass().getName(), new Class<?>[0], new Object[0], Instantiator.class);
         destroyers.get(0).invoke(destroyer, holder.retrieve(typeClass));
         holder.remove(typeClass);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to destroy Selenium driver", e);
      }
   }

}
