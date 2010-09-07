package org.jboss.arquillian.selenium.meta;

public class SystemPropertiesConfiguration implements Configuration
{

   public int getInt(String key, int defaultValue)
   {
      String value = SecurityActions.getProperty(key);
      if (value == null)
      {
         return defaultValue;
      }
      return Integer.parseInt(value);
   }

   public String getString(String key, String defaultValue)
   {
      return SecurityActions.getProperty(key, defaultValue);
   }

   public int getInt(String key)
   {
      return Integer.parseInt(SecurityActions.getProperty(key));
   }

   public String getString(String key)
   {
      return SecurityActions.getProperty(key);
   }

}
