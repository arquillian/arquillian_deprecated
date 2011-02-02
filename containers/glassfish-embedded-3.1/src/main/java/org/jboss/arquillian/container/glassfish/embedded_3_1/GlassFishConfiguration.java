/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.glassfish.embedded_3_1;

import org.jboss.arquillian.spi.client.container.ContainerConfiguration;

/**
 * GlassfishConfiguration
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class GlassFishConfiguration implements ContainerConfiguration
{
   private int bindHttpPort = 8181;
   private String instanceRoot = null;
   private String installRoot = null;
   private boolean configurationReadOnly = true;
   private String configurationXml;
   private String sunResourcesXml;
   
   public int getBindHttpPort()
   {
      return bindHttpPort;
   }

   public void setBindHttpPort(int bindHttpPort)
   {
      this.bindHttpPort = bindHttpPort;
   }

   public String getInstanceRoot() 
   {
      return instanceRoot;
   }
   
   public void setInstanceRoot(String instanceRoot)
   {
      this.instanceRoot = instanceRoot;
   }
   
   public String getInstallRoot()
   {
      return installRoot;
   }
   
   public void setInstallRoot(String installRoot)
   {
      this.installRoot = installRoot;
   }
   
   /**
    * @return the configurationReadOnly
    */
   public boolean isConfigurationReadOnly()
   {
      return configurationReadOnly;
   }
   
   /**
    * @param configurationReadOnly the configurationReadOnly to set
    */
   public void setConfigurationReadOnly(boolean configurationReadOnly)
   {
      this.configurationReadOnly = configurationReadOnly;
   }

   public String getConfigurationXml()
   {
      return configurationXml;
   }

   public void setConfigurationXml(String configurationXml)
   {
      this.configurationXml = configurationXml;
   }

   public String getSunResourcesXml()
   {
      return sunResourcesXml;
   }

   public void setSunResourcesXml(String sunResourcesXml)
   {
      this.sunResourcesXml = sunResourcesXml;
   }

}
