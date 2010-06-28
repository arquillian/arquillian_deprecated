package org.jboss.arquillian.container.jetty.embedded_6_1;

import java.net.URL;
import org.mortbay.jetty.plus.webapp.Configuration;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;

/**
 * A Jetty {@link Configuration} object that gathers configuration from
 * WEB-INF/web.xml, annotations, an override descriptor, an exactly one
 * META-INF/web-fragment.xml. Most of the code is copied from
 * the standard and plus configurations, augmented to search for
 * a META-INF/web-fragment.xml resource (though multiple entries
 * should be supported in the future).
 *
 * @author Dan Allen
 */
public class CompositeConfiguration extends Configuration
{
   @Override
   public void configureWebApp() throws Exception
   {
      // logic from WebXmlConfiguration
      if (_context.isStarted())
      {
         if (Log.isDebugEnabled())
         {
            Log.debug("Cannot configure webapp after it is started");
         }
         return;
      }

      URL webxml = findWebXml();
      if (webxml != null)
      {
         configure(webxml.toString());
      }

      String overrideDescriptor = getWebAppContext().getOverrideDescriptor();
      if (overrideDescriptor != null && overrideDescriptor.length() > 0)
      {
         Resource orideResource = Resource.newSystemResource(overrideDescriptor);
         if (orideResource == null)
         {
            orideResource = Resource.newResource(overrideDescriptor);
         }
         _xmlParser.setValidating(false);
         configure(orideResource.getURL().toString());
      }

      // FIXME support multiple web-fragment.xml files
      Resource webFragment = Resource.newSystemResource("META-INF/web-fragment.xml");
      if (webFragment != null)
      {
         _xmlParser.setValidating(false);
         configure(webFragment.getURL().toString());
      }

      // logic from AbstractConfiguration
      bindUserTransaction();

      // logic from Configuration
      //lock this webapp's java:comp namespace as per J2EE spec
      ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getWebAppContext().getClassLoader());
      lockCompEnv();
      Thread.currentThread().setContextClassLoader(oldLoader);
   }
}
