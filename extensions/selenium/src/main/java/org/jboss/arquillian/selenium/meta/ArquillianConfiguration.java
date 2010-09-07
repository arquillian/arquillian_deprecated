package org.jboss.arquillian.selenium.meta;

/**
 * A configuration backed by Arquillian configuration
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class ArquillianConfiguration implements Configuration
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
      return defaultValue;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.selenium.meta.Configuration#getInt(java.lang.String)
    */
   public int getInt(String key)
   {
      return -1;
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
      return null;
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
      return null;
   }

}
