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
package org.jboss.arquillian.osgi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.jboss.arquillian.protocol.local.LocalMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.arquillian.spi.event.container.AfterDeploy;
import org.jboss.arquillian.spi.event.container.BeforeSetup;
import org.jboss.logging.Logger;
import org.jboss.osgi.framework.bundle.OSGiBundleManager;
import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.osgi.testing.OSGiTestHelper;
import org.jboss.osgi.vfs.AbstractVFS;
import org.jboss.osgi.vfs.VirtualFile;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * [TODO] OSGiEmbeddedContainer
 *
 * @author Thomas.Diesler@jboss.com
 * @version $Revision: $
 */
public class OSGiEmbeddedContainer implements DeployableContainer
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGiEmbeddedContainer.class);
   
   private Framework framework;
   
   @Override
   public void setup(Context context, Configuration configuration)
   {
      OSGiBootstrapProvider provider = OSGiBootstrap.getBootstrapProvider();
      framework = provider.getFramework();
   }
   
   @Override
   public void start(Context context) throws LifecycleException
   {
      try
      {
         framework.start();
      }
      catch (BundleException ex)
      {
         throw new LifecycleException("Cannot start embedded OSGi Framework", ex);
      }
   }

   @Override
   public void stop(Context context) throws LifecycleException
   {
      try
      {
         framework.stop();
         framework.waitForStop(3000);
      }
      catch (Exception ex)
      {
         log.error("Cannot stop embedded OSGi Framework", ex);
      }
   }

   @Override
   public ContainerMethodExecutor deploy(Context context, final Archive<?> archive)  throws DeploymentException
   {
      VirtualFile virtualFile = toVirtualFile(archive);
      BundleContext sysContext = framework.getBundleContext();
      sysContext.installBundle(archive.getName(), virtualFile.openStream());
      return new LocalMethodExecutor();
   }

   @Override
   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
   }

   private VirtualFile toVirtualFile(Archive<?> archive) throws IOException, MalformedURLException
   {
      ZipExporter exporter = archive.as(ZipExporter.class);
      File target = File.createTempFile("osgi-bundle_", ".jar");
      exporter.exportZip(target, true);
      target.deleteOnExit();
      return AbstractVFS.getRoot(target.toURI().toURL());
   }
}