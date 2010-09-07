package org.jboss.arquillian.selenium.meta;

/**
 * A configuration backed by Java System properties
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class SystemPropertiesConfiguration implements Configuration
{

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.meta.Configuration#getInt(java.lang.String,
    * int)
    */
   public int getInt(String key, int defaultValue)
   {
      String value = SecurityActions.getProperty(key);
      if (value == null)
      {
         return defaultValue;
      }
      return Integer.parseInt(value);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.meta.Configuration#getString(java.lang.String
    * , java.lang.String)
    */
   public String getString(String key, String defaultValue)
   {
      return SecurityActions.getProperty(key, defaultValue);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.meta.Configuration#getInt(java.lang.String)
    */
   public int getInt(String key)
   {
      String value = SecurityActions.getProperty(key);

      if (value == null)
         return -1;

      return Integer.parseInt(value);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.meta.Configuration#getString(java.lang.String
    * )
    */
   public String getString(String key)
   {
      return SecurityActions.getProperty(key);
   }

}
