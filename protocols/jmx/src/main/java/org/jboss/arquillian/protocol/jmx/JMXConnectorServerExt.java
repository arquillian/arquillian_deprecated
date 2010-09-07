/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.protocol.jmx;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.jboss.logging.Logger;

/**
 * A RMI/JRMP connector service.
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class JMXConnectorServerExt
{
   // Provide logging
   private static final Logger log = Logger.getLogger(JMXConnectorServerExt.class);

   private JMXServiceURL serviceURL;
   private JMXConnectorServer jmxConnectorServer;
   private boolean shutdownRegistry;
   private Registry rmiRegistry;

   public JMXConnectorServerExt(JMXServiceURL serviceURL, int regPort) throws IOException
   {
      this.serviceURL = serviceURL;

      String host = serviceURL.getHost();
      
      // Check to see if registry already created
      rmiRegistry = LocateRegistry.getRegistry(host, regPort);
      try
      {
         rmiRegistry.list();
         log.debug("RMI registry running at host=" + host + ",port=" + regPort);
      }
      catch (Exception ex)
      {
         log.debug("No RMI registry running at host=" + host + ",port=" + regPort + ".  Will create one.");
         rmiRegistry = LocateRegistry.createRegistry(regPort, null, new DefaultSocketFactory(InetAddress.getByName(host)));
         shutdownRegistry = true;
      }
   }

   public void start(MBeanServer mbeanServer) throws IOException
   {
      // create new connector server and start it
      jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(serviceURL, null, mbeanServer);
      log.debug("JMXConnectorServer created: " + serviceURL);

      jmxConnectorServer.start();
      log.debug("JMXConnectorServer started: " + serviceURL);
   }

   public void stop()
   {
      try
      {
         jmxConnectorServer.stop();

         // Shutdown the registry if this service created it
         if (shutdownRegistry == true)
         {
            log.debug("Shutdown RMI registry");
            UnicastRemoteObject.unexportObject(rmiRegistry, true);
         }

         log.debug("JMXConnectorServer stopped");
      }
      catch (IOException ex)
      {
         log.warn("Cannot stop JMXConnectorServer", ex);
      }
   }
}