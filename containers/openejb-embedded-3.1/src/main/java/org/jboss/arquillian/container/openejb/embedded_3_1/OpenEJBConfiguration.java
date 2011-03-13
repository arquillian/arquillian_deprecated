/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.openejb.embedded_3_1;

import java.io.File;

import org.jboss.arquillian.spi.ConfigurationException;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;

/**
 * OpenEJBConfiguration
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @author David Allen
 * @version $Revision: $
 */
public class OpenEJBConfiguration implements ContainerConfiguration
{
   private String openEjbXml;
   
   private String jndiProperties;

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.ContainerConfiguration#validate()
    */
   @Override
   public void validate() throws ConfigurationException
   {
      // Verify that the jndi.properties file exists, if specified
      if (jndiProperties != null)
      {
         if (!new File(jndiProperties).canRead())
         {
            throw new ConfigurationException("Cannot locate the jndi.properties file " + jndiProperties);
         }
      }
      // Verify that the openejb.xml resource exists, if specified
      if (openEjbXml != null)
      {
         if (!new File(openEjbXml).canRead())
         {
            throw new ConfigurationException("Cannot locate OpenEJB Configuration file " + openEjbXml);
         }
      }
   }

   public String getJndiProperties()
   {
      return jndiProperties;
   }

   public void setJndiProperties(String jndiProperties)
   {
      this.jndiProperties = jndiProperties;
   }

   public String getOpenEjbXml()
   {
      return openEjbXml;
   }

   public void setOpenEjbXml(String openEjbXml)
   {
      this.openEjbXml = openEjbXml;
   }
}
