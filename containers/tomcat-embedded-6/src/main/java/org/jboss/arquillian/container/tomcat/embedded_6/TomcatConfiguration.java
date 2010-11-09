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
package org.jboss.arquillian.container.tomcat.embedded_6;

import org.jboss.arquillian.spi.ContainerProfile;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;

/**
 * Arquillian Tomcat Container Configuration
 * 
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @author Dan Allen
 * @version $Revision: $
 */
public class TomcatConfiguration implements ContainerConfiguration
{
   private String bindAddress = "localhost";

   private int bindHttpPort = 8080;

   private String tomcatHome = null;

   private String appBase = "webapps";

   private String workDir = null;

   private String serverName = "arquillian-tomcat-embedded-6";
   
   private boolean unpackArchive = false;

   public ContainerProfile getContainerProfile()
   {
      return ContainerProfile.CLIENT;
   }

   public String getBindAddress()
   {
      return bindAddress;
   }

   public void setBindAddress(String bindAddress)
   {
      this.bindAddress = bindAddress;
   }

   public int getBindHttpPort()
   {
      return bindHttpPort;
   }

   /**
    * Set the HTTP bind port.
    *
    * @param httpBindPort
    *            HTTP bind port
    */
   public void setBindHttpPort(int bindHttpPort)
   {
      this.bindHttpPort = bindHttpPort;
   }

   public void setTomcatHome(String jbossHome)
   {
      this.tomcatHome = jbossHome;
   }

   public String getTomcatHome()
   {
      return tomcatHome;
   }

   /**
    * @param appBase the directory where the deployed webapps are stored within the Tomcat installation
    */
   public void setAppBase(String tomcatAppBase)
   {
      this.appBase = tomcatAppBase;
   }

   public String getAppBase()
   {
      return appBase;
   }

   /**
    * @param workDir the directory where the compiled JSP files and session serialization data is stored
    */
   public void setWorkDir(String tomcatWorkDir)
   {
      this.workDir = tomcatWorkDir;
   }

   public String getTomcatWorkDir()
   {
      return workDir;
   }

   /**
    * @param serverName the serverName to set
    */
   public void setServerName(String serverName)
   {
      this.serverName = serverName;
   }

   /**
    * @return the serverName
    */
   public String getServerName()
   {
      return serverName;
   }

   /**
    * @return a switch indicating whether the WAR should be unpacked
    */
   public boolean isUnpackArchive()
   {
      return unpackArchive;
   }

   /**
    * Sets the WAR to be unpacked into the java.io.tmpdir when deployed.
    * Unpacking is required if you are using Weld to provide CDI support
    * in a servlet environment.
    *
    * @param a switch indicating whether the WAR should be unpacked
    */
   public void setUnpackArchive(boolean unpack)
   {
      this.unpackArchive = unpack;
   }
}
