package org.jboss.arquillian.selenium.meta;

/**
 * A generic way how to obtain configuration parameters from an arbitrary
 * key-value storage
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public interface Configuration
{
   /**
    * Retrieves a string mapped by key
    * 
    * @param key the key
    * @return The value or {@code null} if no such key is mapped
    */
   String getString(String key);

   /**
    * Retrieves a string mapped by key
    * 
    * @param key the key
    * @param defaultValue The value returned if no such key is mapped
    * @return The value or {@code defaultValue} is no such key is mapped
    */
   String getString(String key, String defaultValue);

   /**
    * Retrieves an integer mapped by key
    * 
    * @param key the key
    * @param defaultValue The value returned if no such key is mapped
    * @return The value or {@code defaultValue} is no such key is mapped
    * @throws NumberFormatException If value mapped by key does not represent an
    *            integer
    */
   int getInt(String key, int defaultValue);

   /**
    * Retrieves an integer mapped by key
    * 
    * @param key the key
    * @return The value or {@code -1} if no such key is mapped
    */
   int getInt(String key);

}
