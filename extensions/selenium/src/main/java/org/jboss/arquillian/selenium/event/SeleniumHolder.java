package org.jboss.arquillian.selenium.event;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds Selenium object in cache. It is used to store Selenium context between
 * test method calls in Arquillian testing context.
 * 
 * Generic approach allows to have an arbitrary implementation of Selenium,
 * varying from Selenium WebDriver to Cheiron.
 * 
 * Current implementation limits occurrence of the testing browser to one per
 * class.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class SeleniumHolder
{
   // cache holder
   private Map<Class<?>, Object> cache = new HashMap<Class<?>, Object>();

   /**
    * Stores an instance of Selenium in the holder.
    * 
    * @param clazz The class of the instance store
    * @param instance The instance to be stored
    */
   public void hold(Class<?> clazz, Object instance)
   {
      cache.put(clazz, instance);
   }

   /**
    * Retrieves instance stored in holder by key {@code clazz}
    * 
    * @param <T> Type of the instance stored
    * @param clazz The key used to find the instance
    * @return The instance if found or {@code null} otherwise
    */
   public <T> T retrieve(Class<T> clazz)
   {
      return clazz.cast(cache.get(clazz));
   }

   /**
    * Checks if there is already an instance stored by {@code clazz} in the
    * holder
    * 
    * @param clazz The class to be checked
    * @return {@code true} if there is such instance, {@code false} otherwise
    */
   public boolean contains(Class<?> clazz)
   {
      return cache.containsKey(clazz);
   }

   /**
    * Removes the instance from holder
    * 
    * @param clazz Key of instance which should be removed
    */
   public void remove(Class<?> clazz)
   {
      cache.remove(clazz);
   }

}
