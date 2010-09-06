package org.jboss.arquillian.selenium.event;

import java.lang.reflect.Field;
import java.util.List;

import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.event.suite.BeforeClass;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * A handler which creates a Selenium browser, Selenium WebDriver or Cheiron implementation and
 * binds it to the current context. The instance is stored in {@link SeleniumHolder}, 
 * which defines default implementation if no configuration is found in arquillian.xml file<br/>
 * <br/>  
 *  <b>Imports:</b><br/>
 *   {@link Selenium}<br/>
 *  <br/>
 *  <b>Exports:</b><br/>
 *   {@link SeleniumHolder}</br>
 *  <br/>
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
      try
      {
         for (Field f : fields)
         {
            f.setAccessible(true);
            createAndStoreSelenium(holder, f.getType());
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not  members", e);
      }
      context.add(SeleniumHolder.class, holder);
   }

   private <T> void createAndStoreSelenium(SeleniumHolder holder, Class<T> type)
   {
      // there's already an instance mapped
      if (holder.contains(type))
         return;

      // FIXME
      // see ARQ-215 for possibility to configure constructor

      // retrieve default implementation
      T browser = createDefaultImplementation(type);
      holder.hold(type, browser);

   }

   private <T> T createDefaultImplementation(Class<T> type)
   {
      String className = type.getName();
      if (SeleniumHolder.DEFAULT_SELENIUM_IMPLEMENTATION_CLASS.equals(className) || SeleniumHolder.DEFAULT_SELENIUM_CLASS.equals(className))
      {
         return SecurityActions.newInstance(SeleniumHolder.DEFAULT_SELENIUM_IMPLEMENTATION_CLASS, new Class<?>[0], new Object[0], type);
      }

      throw new RuntimeException("The field annotated by @Selenium is of type incompatible with extension's default implemenation.\nPlease set up a configuration of your Selenium browser in arquillian.xml file.");
   }

}
