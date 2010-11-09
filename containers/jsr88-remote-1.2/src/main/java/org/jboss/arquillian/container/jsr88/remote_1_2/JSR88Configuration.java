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
package org.jboss.arquillian.container.jsr88.remote_1_2;

import org.jboss.arquillian.spi.ContainerProfile;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;

/**
 * A {@link org.jboss.arquillian.spi.client.container.ContainerConfiguration} implementation for
 * a JSR 88-compliant container. The required configuration settings are the
 * deployment URI and the deployment factory class.
 *
 * @author Dan Allen
 * @author Iskandar Salim
 * @see http://jcp.org/en/jsr/detail?id=88
 */
public class JSR88Configuration implements ContainerConfiguration
{
   private int remoteServerHttpPort = 8080;
   private String remoteServerAddress = "localhost";
   private String deploymentUri = "";
   private String deploymentUsername = "";
   private String deploymentPassword = "";
   private String deploymentFactoryClass = "";
   private int deploymentTimeoutSeconds = 5;

   public JSR88Configuration()
   {
      setDefaults();
   }

   protected void setDefaults()
   {
   }

   public int getRemoteServerHttpPort()
   {
      return remoteServerHttpPort;
   }

   public void setRemoteServerHttpPort(int remoteServerHttpPort)
   {
      this.remoteServerHttpPort = remoteServerHttpPort;
   }

   public String getRemoteServerAddress()
   {
      return remoteServerAddress;
   }

   public void setRemoteServerAddress(String remoteServerAddress)
   {
      this.remoteServerAddress = remoteServerAddress;
   }

   public String getDeploymentFactoryClass()
   {
      return deploymentFactoryClass;
   }

   public void setDeploymentFactoryClass(String deploymentFactoryClass)
   {
      this.deploymentFactoryClass = deploymentFactoryClass;
   }

   public String getDeploymentPassword()
   {
      return deploymentPassword;
   }

   public void setDeploymentPassword(String deploymentPassword)
   {
      this.deploymentPassword = deploymentPassword;
   }

   public String getDeploymentUri()
   {
      return deploymentUri;
   }

   public void setDeploymentUri(String deploymentUri)
   {
      this.deploymentUri = deploymentUri;
   }

   public String getDeploymentUsername()
   {
      return deploymentUsername;
   }

   public void setDeploymentUsername(String deploymentUsername)
   {
      this.deploymentUsername = deploymentUsername;
   }

   public int getDeploymentTimeoutSeconds()
   {
      return deploymentTimeoutSeconds;
   }

   public void setDeploymentTimeoutSeconds(int deploymentTimeoutSeconds)
   {
      this.deploymentTimeoutSeconds = deploymentTimeoutSeconds;
   }
}
