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
package org.jboss.arquillian.container.jbossas.managed_6;

import java.io.File;

import org.jboss.arquillian.spi.ContainerConfiguration;
import org.jboss.arquillian.spi.ContainerProfile;

/**
 * A {@link org.jboss.arquillian.spi.ContainerConfiguration} implementation for
 * the JBoss AS container.
 *
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 * @version $Revision: $
 */
public class JBossASConfiguration implements ContainerConfiguration
{
   private String bindAddress = "localhost";

   private int httpPort = 8080;

   private String profileName = "default";
   
   private String jbossHome = System.getenv("JBOSS_HOME");
   
   private String javaHome = System.getenv("JAVA_HOME");
   
   private String javaVmArguments = "-Xmx512m -XX:MaxPermSize=128m";

   // default to ERROR to ensure a fast start, though let it be configurable
   // might need to look into this more because both server.log and output.log are being written
   private String logThreshold = "ERROR";

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

   public int getHttpPort()
   {
      return httpPort;
   }

   /**
    * Set the HTTP Connect port. <br/>
    * This is not the JBoss AS HTTP Bind port, bind port must be set in the JBoss XML configuration.<br/>
    * <b>Only set this if default http port is changed in JBoss AS!</b>
    * 
    * @param httpPort HTTP Connect port
    */
   public void setHttpPort(int httpPort)
   {
      this.httpPort = httpPort;
   }

   public String getProfileName()
   {
      return profileName;
   }

   public void setProfileName(String profileName)
   {
      this.profileName = profileName;
   }

   public void setJbossHome(String jbossHome)
   {
      this.jbossHome = jbossHome;
   }
   
   public String getJbossHome()
   {
      if(jbossHome != null) 
      {
         return new File(jbossHome).getAbsolutePath();
      }
      return jbossHome;
   }
   
   public void setJavaHome(String javaHome)
   {
      this.javaHome = javaHome;
   }
   
   public String getJavaHome()
   {
      return javaHome;
   }
   
   /**
    * This will override the default ("-Xmx512m -XX:MaxPermSize=128m") startup JVM arguments. 
    * 
    * @param javaVmArguments use as start up arguments
    */
   public void setJavaVmArguments(String javaVmArguments)
   {
      this.javaVmArguments = javaVmArguments;
   }
   
   public String getJavaVmArguments()
   {
      return javaVmArguments;
   }

   public String getLogThreshold()
   {
      return logThreshold;
   }

   public void setLogThreshold(String logThreshold)
   {
      this.logThreshold = logThreshold;
   }
}
