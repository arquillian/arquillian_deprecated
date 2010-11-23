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
