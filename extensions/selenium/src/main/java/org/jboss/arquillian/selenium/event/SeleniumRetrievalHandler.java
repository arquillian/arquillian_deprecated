package org.jboss.arquillian.selenium.event;

import java.lang.reflect.Field;
import java.util.List;

import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.jboss.arquillian.spi.event.suite.TestEvent;

/**
 * A handler which sets a cached instance of Selenium browser for fields annotated with {@link Selenium}.  
 * <br/>
 * <b>Imports:</b><br/>
 * {@link Selenium} <br/> 
 * {@link SeleniumHolder} <br/> 
 * <br/>
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @see SeleniumHolder
 * @see Selenium
 */
public class SeleniumRetrievalHandler implements EventHandler<TestEvent>
{

   /*
    * (non-Javadoc)
    * @see org.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, TestEvent event) throws Exception
   {
      injectSelenium(context, event.getTestClass().getJavaClass(), event.getTestInstance());
   }

   private void injectSelenium(Context context, Class<?> clazz, Object testInstance)
   {

      List<Field> fields = SecurityActions.getFieldsWithAnnotation(clazz, Selenium.class);
      SeleniumHolder holder = context.get(SeleniumHolder.class);
      try
      {
         for (Field f : fields)
         {
            f.setAccessible(true);            
            Object value = holder.retrieve(f.getType());
            Validate.notNull(value, "Retrieved a null from context, which is not a valid Selenium browser");
            
            // omit setting if already set
            if (f.get(testInstance) == null)
            {
               f.set(testInstance, value);
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not inject members", e);
      }

   }

}
