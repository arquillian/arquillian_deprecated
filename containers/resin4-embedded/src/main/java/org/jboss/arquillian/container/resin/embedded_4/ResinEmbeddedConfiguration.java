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
package org.jboss.arquillian.container.resin.embedded_4;

import org.jboss.arquillian.spi.ConfigurationException;
import org.jboss.arquillian.spi.ContainerProfile;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;

/**
 * A {@link org.jboss.arquillian.spi.client.container.ContainerConfiguration} implementation for
 * the Resin4 Embedded container.
 *
 * @author Dominik Dorn
 * @author ales.justin@jboss.org
 * @version $Revision: $
 */
public class ResinEmbeddedConfiguration implements ContainerConfiguration
{
   private String bindAddress = "127.0.0.1";
   private String serverId = "arquillian";
   private int bindHttpPort = 9090;

   public void validate() throws ConfigurationException
   {
      // TODO
   }

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

   public String getBindAddress()
   {
      return bindAddress;
   }

   public void setBindAddress(String bindAddress)
   {
      this.bindAddress = bindAddress;
   }

   public String getServerId()
   {
      return serverId;
   }

   public void setServerId(String serverId)
   {
      this.serverId = serverId;
   }
}
