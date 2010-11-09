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
package org.jboss.arquillian.container.jetty.embedded_6_1;

import org.jboss.arquillian.spi.ContainerProfile;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;

/**
 * A {@link org.jboss.arquillian.spi.client.container.ContainerConfiguration} implementation for
 * the Jetty Embedded 6.1.x container.
 *
 * @author Dan Allen
 * @version $Revision: $
 */
public class JettyEmbeddedConfiguration implements ContainerConfiguration
{
   private String bindAddress = "localhost";

   private int bindHttpPort = 9090;

   private boolean jettyPlus = true;

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

   public boolean isJettyPlus()
   {
      return jettyPlus;
   }

   public void setJettyPlus(boolean jettyPlus)
   {
      this.jettyPlus = jettyPlus;
   }
}
