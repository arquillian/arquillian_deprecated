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
package org.jboss.arquillian.glassfish.embedded30;

import java.util.UUID;

import org.jboss.arquillian.spi.ContainerConfiguration;
import org.jboss.arquillian.spi.ContainerProfile;

/**
 * A {@link org.jboss.arquillian.spi.ContainerConfiguration} implementation for
 * the GlassFish Embedded container.
 *
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 * @version $Revision: $
 */
public class GlassFishConfiguration implements ContainerConfiguration
{
   private int bindPort = 8080;
   private String instanceRoot = "target/glassfish_" + UUID.randomUUID().toString();
   private boolean autoDelete = true;
   
   public ContainerProfile getContainerProfile()
   {
      return ContainerProfile.CLIENT;
   }
   
   public int getBindPort()
   {
      return bindPort;
   }

   public void setBindPort(int bindPort)
   {
      this.bindPort = bindPort;
   }

   public String getInstanceRoot() 
   {
      return instanceRoot;
   }
   
   public void setInstanceRoot(String instanceRoot)
   {
      this.instanceRoot = instanceRoot;
   }
   
   public boolean getAutoDelete() 
   {
      return autoDelete;
   }
   
   public void setAutoDelete(boolean autoDelete)
   {
      this.autoDelete = autoDelete;
   }
}
