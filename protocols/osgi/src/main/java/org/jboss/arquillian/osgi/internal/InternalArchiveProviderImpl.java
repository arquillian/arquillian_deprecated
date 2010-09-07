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

import java.io.ByteArrayOutputStream;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.jboss.arquillian.osgi.ArchiveProvider;
import org.jboss.arquillian.protocol.jmx.JMXServerFactory;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * 
 * @author thomas.diesler@jboss.com
 * @since 06-Sep-2010
 */
public class InternalArchiveProviderImpl implements InternalArchiveProvider
{
   // Provide logging
   private static final Logger log = Logger.getLogger(InternalArchiveProviderImpl.class);

   private final ArchiveProvider delegate;
   private final ObjectName objectName;

   public InternalArchiveProviderImpl(TestClass testCase, ArchiveProvider provider)
   {
      this.delegate = provider;

      String name = ONAME_PREFIX + testCase.getSimpleName();
      try
      {
         objectName = new ObjectName(name);
      }
      catch (MalformedObjectNameException e)
      {
         throw new IllegalArgumentException("Invalid object name: " + name);
      }
   }

   public void registerMBean() throws JMException
   {
      MBeanServer mbeanServer = JMXServerFactory.findOrCreateMBeanServer();
      if (mbeanServer.isRegistered(objectName) == false)
      {
         log.debug("Register: " + objectName);
         StandardMBean mbean = new StandardMBean(this, InternalArchiveProvider.class);
         mbeanServer.registerMBean(mbean, objectName);
      }
   }

   public void unregisterMBean()
   {
      MBeanServer mbeanServer = JMXServerFactory.findOrCreateMBeanServer();
      if (mbeanServer.isRegistered(objectName))
      {
         try
         {
            log.debug("Unregister: " + objectName);
            mbeanServer.unregisterMBean(objectName);
         }
         catch (JMException ex)
         {
            log.error("Cannot unregister: " + objectName);
         }
      }
   }

   public byte[] getTestArchive(String name)
   {
      JavaArchive archive = delegate.getTestArchive(name);
      ZipExporter exporter = archive.as(ZipExporter.class);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      exporter.exportZip(baos);
      return baos.toByteArray();
   }
}
