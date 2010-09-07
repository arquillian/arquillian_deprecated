package org.jboss.arquillian.selenium.instantiator;

import org.jboss.arquillian.selenium.annotation.Selenium;

/**
 * The instantiator provides a bridge between Arquillian Selenium extension and
 * arbitrary testing driver. Arquillian Selenium provides instantiators to most
 * common frameworks.
 * 
 * Users which require special functionality can provide their own instantiator
 * and pass it to {@link Selenium} annotation configuration.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 * @param <T> The type of driver used by this instantiator
 * @see Selenium
 */
public interface Instantiator<T>
{
   /**
    * Creates an instance of the driver.
    * 
    * The instance is created before execution of the first method of the test
    * class automatically by calling this method. The object is then bound to
    * the Arquillian context, where it stays until the execution of the last
    * test method is finished.
    * 
    * @return Newly created instance of the driver
    */
   T create();

   /**
    * Destroys an instance of the driver.
    * 
    * After the last method is run, the driver instance is destroyed. This means
    * browser windows, if any, are closed and used resources are freed.
    * 
    * @param instance The instance to be destroyed
    */
   void destroy(T instance);
}
