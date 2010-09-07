package org.jboss.arquillian.selenium.meta;

public interface Configuration
{
   String getString(String key);

   String getString(String key, String defaultValue);

   int getInt(String key, int defaultValue);

   int getInt(String key);

}
