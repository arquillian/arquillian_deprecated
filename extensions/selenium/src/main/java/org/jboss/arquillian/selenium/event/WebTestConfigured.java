/**
 * 
 */
package org.jboss.arquillian.selenium.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.jboss.arquillian.selenium.spi.WebTestConfiguration;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class WebTestConfigured
{
   private Class<? extends Annotation> qualifier;
   private Field injected;
   private WebTestConfiguration<?> configuration;

   /**
    * 
    */
   public WebTestConfigured(Field injected, Class<? extends Annotation> qualifier, WebTestConfiguration<?> configuration)
   {
      this.injected = injected;
      this.qualifier = qualifier;
      this.configuration = configuration;
   }

   /**
    * @return the qualifier
    */
   public Class<? extends Annotation> getQualifier()
   {
      return qualifier;
   }

   /**
    * @param qualifier the qualifier to set
    */
   public void setQualifier(Class<? extends Annotation> qualifier)
   {
      this.qualifier = qualifier;
   }

   /**
    * @return the injected
    */
   public Field getInjected()
   {
      return injected;
   }

   /**
    * @param injected the injected to set
    */
   public void setInjected(Field injected)
   {
      this.injected = injected;
   }

   /**
    * @param configuration the configuration to set
    */
   public void setConfiguration(WebTestConfiguration<?> configuration)
   {
      this.configuration = configuration;
   }

   /**
    * @return the configuration
    */
   public WebTestConfiguration<?> getConfiguration()
   {
      return configuration;
   }

}
