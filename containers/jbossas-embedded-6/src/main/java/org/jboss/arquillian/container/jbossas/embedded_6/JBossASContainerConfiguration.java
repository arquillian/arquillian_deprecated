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
package org.jboss.arquillian.container.jbossas.embedded_6;

import org.jboss.arquillian.spi.ContainerProfile;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;

/**
 * JBossContainerConfiguration
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JBossASContainerConfiguration implements ContainerConfiguration
{
   /**
    * The profile to start. 
    */
   private String profileName = "default";

   /**
    * Address the server should bind to. 
    */
   private String bindAddress = "localhost";
   
   /**
    * Used by Servlet Protocol to connect to the server. 
    */
   private int httpPort = 8080;
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.ContainerConfiguration#getContainerProfile()
    */
   public ContainerProfile getContainerProfile()
   {
      return ContainerProfile.STANDALONE;
   }

   public String getProfileName()
   {
      return profileName;
   }
   
   public void setProfileName(String profileName)
   {
      this.profileName = profileName;
   }
   
   public String getBindAddress()
   {
      return bindAddress;
   }
   
   public void setBindAddress(String bindAddress)
   {
      this.bindAddress = bindAddress;
   }
   
   public int getHttpPort()
   {
      return httpPort;
   }
   
   public void setHttpPort(int httpPort)
   {
      this.httpPort = httpPort;
   }
}
