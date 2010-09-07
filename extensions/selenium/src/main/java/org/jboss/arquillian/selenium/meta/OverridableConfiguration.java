package org.jboss.arquillian.selenium.meta;

public class OverridableConfiguration implements Configuration
{
   private Configuration backup;
   private Configuration master;

   public OverridableConfiguration(Configuration backup, Configuration master)
   {
      this.backup = backup;
      this.master = master;
   }

   public int getInt(String key, int defaultValue)
   {
      String value = getString(key);
      if (value == null)
      {
         return defaultValue;
      }

      return Integer.parseInt(value);
   }

   public int getInt(String key)
   {
      String value = master.getString(key);
      if (value == null)
      {
         return backup.getInt(key);
      }

      return Integer.parseInt(value);
   }

   public String getString(String key)
   {
      String value = master.getString(key);
      if (value == null)
      {
         return backup.getString(key);
      }

      return value;
   }

   public String getString(String key, String defaultValue)
   {
      String value = getString(key);
      if (value == null)
      {
         return defaultValue;
      }

      return value;
   }

}
