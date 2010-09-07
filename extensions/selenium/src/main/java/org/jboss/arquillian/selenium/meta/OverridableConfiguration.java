package org.jboss.arquillian.selenium.meta;

/**
 * A configuration which user two distinct configurations, where the second
 * takes precedence. This can be used to have two different means of
 * configuration with a priority defined between them.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class OverridableConfiguration implements Configuration
{
   private Configuration backup;
   private Configuration master;

   /**
    * Creates a configuration
    * 
    * @param backup The configuration for value retrieval in master is not found
    * @param master The master configuration, which takes precedence
    */
   public OverridableConfiguration(Configuration backup, Configuration master)
   {
      this.backup = backup;
      this.master = master;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.meta.Configuration#getInt(java.lang.String,
    * int)
    */
   public int getInt(String key, int defaultValue)
   {
      String value = getString(key);
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
    * org.jboss.arquillian.selenium.meta.Configuration#getInt(java.lang.String)
    */
   public int getInt(String key)
   {
      String value = master.getString(key);
      if (value == null)
      {
         return backup.getInt(key);
      }

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
      String value = master.getString(key);
      if (value == null)
      {
         return backup.getString(key);
      }

      return value;
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
      String value = getString(key);
      if (value == null)
      {
         return defaultValue;
      }

      return value;
   }

}
