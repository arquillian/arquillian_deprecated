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
package org.jboss.arquillian.osgi.internal;

import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_JMX_HOST;
import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_JMX_RMI_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.DEFAULT_REMOTE_JMX_RMI_REGISTRY_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_HOST;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_RMI_PORT;
import static org.jboss.osgi.jmx.JMXConstantsExt.REMOTE_JMX_RMI_REGISTRY_PORT;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jboss.arquillian.osgi.OSGiContainer;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.JMXServiceURLFactory;
import org.osgi.framework.BundleContext;

/**
 * The embedded version of {@link OSGiContainer}
 *
 * @author thomas.diesler@jboss.com
 * @since 07-Sep-2010
 */
public class RemoteOSGiContainer extends AbstractOSGiContainer
{
   // Provide logging
   private static final Logger log = Logger.getLogger(RemoteOSGiContainer.class);
   
   private JMXConnector jmxConnector;

   public RemoteOSGiContainer(BundleContext context, TestClass testClass)
   {
      super(context, testClass);
   }

   @Override
   public MBeanServerConnection getMBeanServerConnection()
   {
      BundleContext context = getBundleContext();
      
      // Obtain the connection properties from the framework
      String jmxHost = context.getProperty(REMOTE_JMX_HOST);
      if (jmxHost == null)
         jmxHost = DEFAULT_REMOTE_JMX_HOST;
      
      String jmxPort = context.getProperty(REMOTE_JMX_RMI_PORT);
      if (jmxPort == null)
         jmxPort = DEFAULT_REMOTE_JMX_RMI_PORT;
      
      String rmiPort = context.getProperty(REMOTE_JMX_RMI_REGISTRY_PORT);
      if (rmiPort == null)
         rmiPort = DEFAULT_REMOTE_JMX_RMI_REGISTRY_PORT;
      
      JMXServiceURL serviceURL = JMXServiceURLFactory.getServiceURL(jmxHost, Integer.parseInt(jmxPort + 1), Integer.parseInt(rmiPort + 1));
      try
      {
         if (jmxConnector == null)
         {
            log.debug("Connecting JMXConnector to: " + serviceURL);
            jmxConnector = JMXConnectorFactory.connect(serviceURL, null);
         }
         
         return jmxConnector.getMBeanServerConnection();
      }
      catch (IOException ex)
      {
         throw new IllegalStateException("Cannot obtain MBeanServerConnection to: " + serviceURL, ex);
      }
   }
}
