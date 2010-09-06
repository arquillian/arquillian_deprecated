package org.jboss.arquillian.selenium.event;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.selenium.instantiator.Instantiator;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * A handler which destroys a Selenium browser, Selenium WebDriver or Cheiron
 * instance from the current context. <br/><br/>
 * <b>Imports:</b><br/> {@link Selenium}<br/> 
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
      List<Field> fields = SecurityActions.getFieldsWithAnnotation(testClass.getJavaClass(), Selenium.class);
      SeleniumHolder holder = context.get(SeleniumHolder.class);
      for (Field f : fields)
      {
         Class<?> typeClass = f.getType();
         if (!holder.contains(typeClass))
            break;

         Selenium annotation = f.getAnnotation(Selenium.class);
         Class<?> instantiatorClass = annotation.instantiator();
         destroySelenium(holder, instantiatorClass, typeClass);
      }
   }

   private void destroySelenium(SeleniumHolder holder, Class<?> instantiatorClass, Class<?> typeClass)
   {
      Method destroyer = null;
      for (Method m : instantiatorClass.getMethods())
      {
         Class<?>[] types = m.getParameterTypes();
         if ("destroy".equals(m.getName()) && types.length == 1 && typeClass.isAssignableFrom(types[0]))
         {
            if (destroyer == null)
               destroyer = m;
            else
               throw new RuntimeException("Could not determine which destroyer method should be used to destroy object of type " + typeClass.getName() + " because there are multiple methods available.");
         }
      }
      Validate.notNull(destroyer, "No destroyer method was found for object of type " + typeClass.getName());

      try
      {
         Instantiator<?> instantiator = SecurityActions.newInstance(instantiatorClass.getName(), new Class<?>[0], new Object[0], Instantiator.class);
         destroyer.invoke(instantiator, holder.retrieve(typeClass));
         holder.remove(typeClass);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to destroy Selenium driver", e);
      }
   }

}
