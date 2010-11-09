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
package org.jboss.arquillian.container.glassfish.embedded_3;

import java.util.UUID;

import org.jboss.arquillian.spi.ContainerProfile;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;

/**
 * A {@link org.jboss.arquillian.spi.client.container.ContainerConfiguration} implementation for
 * the GlassFish Embedded container.
 *
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 * @author Dan Allen
 * @version $Revision: $
 */
public class GlassFishConfiguration implements ContainerConfiguration
{
   private int bindHttpPort = 8181;
   private String instanceRoot = "target/glassfish_" + UUID.randomUUID().toString();
   private boolean autoDelete = true;
   private String domainXml;
   private String sunResourcesXml;
   
   public ContainerProfile getContainerProfile()
   {
      return ContainerProfile.CLIENT;
   }
   
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
   
   public boolean isAutoDelete()
   {
      return autoDelete;
   }
   
   public void setAutoDelete(boolean autoDelete)
   {
      this.autoDelete = autoDelete;
   }

   public String getDomainXml()
   {
      return domainXml;
   }

   public void setDomainXml(String domainXml)
   {
      this.domainXml = domainXml;
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
