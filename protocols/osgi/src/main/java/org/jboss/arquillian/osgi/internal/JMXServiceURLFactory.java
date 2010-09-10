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
package org.jboss.arquillian.osgi.internal;

//$Id: JMXConnectorService.java 103656 2010-04-07 20:02:31Z thomas.diesler@jboss.com $

import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_JMX_HOST;
import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_JMX_RMI_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_JMX_RMI_REGISTRY_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_HOST;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_RMI_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_RMI_REGISTRY_PORT;

import java.net.MalformedURLException;

import javax.management.remote.JMXServiceURL;

/**
 * A factory for the JMXServiceURL
 * 
 * @author thomas.diesler@jboss.com
 * @since 07-Apr-2010
 */
public abstract class JMXServiceURLFactory
{
   public static JMXServiceURL getServiceURL(String host, Integer jmxPort, Integer rmiPort, String path)
   {
      if (host == null)
         host = System.getProperty(REMOTE_JMX_HOST, DEFAULT_REMOTE_JMX_HOST);

      if (jmxPort == null)
         jmxPort = Integer.parseInt(System.getProperty(REMOTE_JMX_RMI_PORT, DEFAULT_REMOTE_JMX_RMI_PORT));

      if (rmiPort == null)
         rmiPort = Integer.parseInt(System.getProperty(REMOTE_JMX_RMI_REGISTRY_PORT, DEFAULT_REMOTE_JMX_RMI_REGISTRY_PORT));

      String jmxConnectorURL = "service:jmx:rmi://" + host + ":" + jmxPort + "/jndi/rmi://" + host + ":" + rmiPort + "/" + path;
      try
      {
         return new JMXServiceURL(jmxConnectorURL);
      }
      catch (MalformedURLException e)
      {
         throw new IllegalArgumentException("Invalid connector URL: " + jmxConnectorURL);
      }
   }
}