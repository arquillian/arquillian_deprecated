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
package org.jboss.arquillian.container.jbossas.remote_6;

import org.jboss.arquillian.spi.ContainerConfiguration;
import org.jboss.arquillian.spi.ContainerProfile;

/**
 * A {@link org.jboss.arquillian.spi.ContainerConfiguration} implementation for
 * the JBoss AS container.
 *
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JBossASConfiguration implements ContainerConfiguration
{
   /**
    * ProfileService profileKey. Used to load the correct profile into the DeploymentManager.  
    */
   private String profileName = "default";
      
   /**
    * Used by Servlet Protocol to connect to deployment.
    * // TODO: these belongs to the configuration of Servlet Protocol. Extract out. 
    */
   private String remoteServerAddress = "localhost";

   /**
    * Used by Servlet Protocol to connect to deployment.
    */
   private int remoteServerHttpPort = 8080;

   /**
    * Bind Address for HTTP server for serving deployments to the remote server.
    * Address should be reachable from remote server. 
    */
   private String localDeploymentBindAddress = "localhost";
   
   /**
    * Bind Port for HTTP server for serving deployments to remote server.
    * Port must be reachable from remote server.
    */
   private int localDeploymentBindPort = 9999;

   /**
    * Flag indicating whether the archive should be exploded when^M
    * it is deployed to the server.^M
    */
   private boolean deployExploded = false;
   
   public ContainerProfile getContainerProfile()
   {
      return ContainerProfile.CLIENT;
   }
   
   public String getProfileName()
   {
      return profileName;
   }
   
   public void setProfileName(String profileName)
   {
      this.profileName = profileName;
   }

   public String getRemoteServerAddress()
   {
      return remoteServerAddress;
   }

   public void setRemoteServerAddress(String remoteServerAddress)
   {
      this.remoteServerAddress = remoteServerAddress;
   }

   public int getRemoteServerHttpPort()
   {
      return remoteServerHttpPort;
   }

   public void setRemoteServerHttpPort(int remoteServerHttpPort)
   {
      this.remoteServerHttpPort = remoteServerHttpPort;
   }
   
   public String getLocalDeploymentBindAddress()
   {
      return localDeploymentBindAddress;
   }
   
   public void setLocalDeploymentBindAddress(String localDeploymentBindAddress)
   {
      this.localDeploymentBindAddress = localDeploymentBindAddress;
   }
   
   public int getLocalDeploymentBindPort()
   {
      return localDeploymentBindPort;
   }
   
   public void setLocalDeploymentBindPort(int localDeploymentBindPort)
   {
      this.localDeploymentBindPort = localDeploymentBindPort;
   }

   public boolean isDeployExploded()
   {
      return deployExploded;
   }

   public void setDeployExploded(boolean deployExploded)
   {
      this.deployExploded = deployExploded;
   }
}
