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
package org.jboss.arquillian.protocol.jmx;

import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.logging.Logger;

/**
 * An arquillian MBeanServerFactory
 * 
 * @author thomas.diesler@jboss.com
 * @since 06-Sep-2010
 */
public final class JMXServerFactory
{
   // Provide logging
   private static Logger log = Logger.getLogger(JMXServerFactory.class);

   // Hide ctor
   private JMXServerFactory()
   {
   }

   /**
    * Find or create the MBeanServer
    */
   public static MBeanServer findOrCreateMBeanServer()
   {
      MBeanServer mbeanServer = null;

      ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
      if (serverArr.size() > 1)
         log.warn("Multiple MBeanServer instances: " + serverArr);

      if (serverArr.size() > 0)
         mbeanServer = serverArr.get(0);

      if (mbeanServer == null)
      {
         log.debug("No MBeanServer, create one ...");
         mbeanServer = MBeanServerFactory.createMBeanServer();
      }

      return mbeanServer;
   }

}
