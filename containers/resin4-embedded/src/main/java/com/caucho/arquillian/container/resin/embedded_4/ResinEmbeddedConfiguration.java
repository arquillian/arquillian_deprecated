/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Dominik Dorn
 */
package com.caucho.arquillian.container.resin.embedded_4;

import org.jboss.arquillian.spi.ContainerConfiguration;
import org.jboss.arquillian.spi.ContainerProfile;

/**
 * A {@link org.jboss.arquillian.spi.ContainerConfiguration} implementation for
 * the Resin4 Embedded container.
 *
 * @author Dominik Dorn
 * @version $Revision: $
 */
public class ResinEmbeddedConfiguration implements ContainerConfiguration
{
   private String bindAddress = "127.0.0.1";
   private String serverId = "arquillian";
   private int bindHttpPort = 9090;

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

   public String getServerId() {
       return serverId;
   }

   public void setServerId(String serverId) {
       this.serverId = serverId;
   }
}
