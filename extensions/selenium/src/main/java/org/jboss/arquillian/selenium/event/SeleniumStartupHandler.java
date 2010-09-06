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
 * A handler which creates a Selenium browser, Selenium WebDriver or Cheiron
 * implementation and binds it to the current context. The instance is stored in
 * {@link SeleniumHolder}, which defines default implementation if no
 * configuration is found in arquillian.xml file<br/>
 * <br/>
 * <b>Imports:</b><br/> {@link Selenium}<br/>
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
      SeleniumHolder holder = new SeleniumHolder();
      List<Field> fields = SecurityActions.getFieldsWithAnnotation(testClass.getJavaClass(), Selenium.class);
      for (Field f : fields)
      {
         Class<?> typeClass = f.getType();
         if (holder.contains(typeClass))
            break;

         Selenium annotation = f.getAnnotation(Selenium.class);
         Class<?> instantiatorClass = annotation.instantiator();
         createAndStoreSelenium(holder, instantiatorClass, typeClass);
      }

      context.add(SeleniumHolder.class, holder);
   }

   private void createAndStoreSelenium(SeleniumHolder holder, Class<?> instantiatorClass, Class<?> typeClass)
   {
      Method creator = null;
      for (Method m : instantiatorClass.getMethods())
      {
         if ("create".equals(m.getName()) && typeClass.isAssignableFrom(m.getReturnType()))
         {
            if (creator == null)
               creator = m;
            else
               throw new RuntimeException("Could not determine which instantiator method should be used to create object of type " + typeClass.getName() + " because there are multiple methods available.");
         }
      }
      Validate.notNull(creator, "No instantiator method was found for object of type " + typeClass.getName());

      try
      {
         Instantiator<?> instantiator = SecurityActions.newInstance(instantiatorClass.getName(), new Class<?>[0], new Object[0], Instantiator.class);
         holder.hold(typeClass, typeClass.cast(creator.invoke(instantiator, new Object[0])));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to instantiate Selenium driver", e);
      }
   }
}
