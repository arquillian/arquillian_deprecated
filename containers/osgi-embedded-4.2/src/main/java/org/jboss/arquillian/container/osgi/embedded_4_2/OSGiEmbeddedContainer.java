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
package org.jboss.arquillian.container.osgi.embedded_4_2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ContainerScoped;
import org.jboss.arquillian.spi.core.annotation.DeploymentScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.logging.Logger;
import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * OSGiEmbeddedContainer
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class OSGiEmbeddedContainer implements DeployableContainer<OSGiEmbeddedConfiguration>
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGiEmbeddedContainer.class);

   @Inject @ContainerScoped
   private InstanceProducer<Framework> frameworkInst;

   @Inject @ContainerScoped
   private InstanceProducer<BundleContext> bundleContextInst;
   
   @Inject @DeploymentScoped
   private InstanceProducer<Bundle> bundleInst;

   @Override
   public Class<OSGiEmbeddedConfiguration> getConfigurationClass()
   {
      return OSGiEmbeddedConfiguration.class;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getDefaultProtocol()
    */
   @Override
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("JMX");
   }
   
   public void setup(OSGiEmbeddedConfiguration configuration)
   {
      OSGiBootstrapProvider provider = OSGiBootstrap.getBootstrapProvider();
      frameworkInst.set(provider.getFramework());
   }

   public void start() throws LifecycleException
   {
      try
      {
         Framework framework = frameworkInst.get();
         framework.start();
         bundleContextInst.set(framework.getBundleContext());
         
         Bundle[] bundles = framework.getBundleContext().getBundles();
         if (getInstalledBundle(bundles, "osgi.cmpn") == null)
            installBundle("org.osgi.compendium", false);
         
         if (getInstalledBundle(bundles, "arquillian-protocol-jmx-osgi-bundle") == null) 
            installBundle("arquillian-protocol-jmx-osgi-bundle", true);
      }
      catch (BundleException ex)
      {
         throw new LifecycleException("Cannot start embedded OSGi Framework", ex);
      }
   }

   public void stop() throws LifecycleException
   {
      try
      {
         Framework framework = frameworkInst.get();
         framework.stop();
         framework.waitForStop(3000);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new LifecycleException("Cannot stop embedded OSGi Framework", ex);
      }
   }

   public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException
   {
      try
      {
         // Export the bundle bytes
         ZipExporter exporter = archive.as(ZipExporter.class);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         exporter.exportTo(baos);
         
         ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
         
         BundleContext sysContext = frameworkInst.get().getBundleContext();
         Bundle bundle = sysContext.installBundle(archive.getName(), inputStream);
         bundleInst.set(bundle);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new DeploymentException("Cannot deploy: " + archive, ex);
      }
      
      return new ProtocolMetaData();
   }

   public void undeploy(Archive<?> archive) throws DeploymentException
   {
      try
      {
         Bundle bundle = bundleInst.get();
         if (bundle != null)
            bundle.uninstall();
      }
      catch (BundleException ex)
      {
         log.error("Cannot undeploy: " + archive, ex);
      }
   }

   @Override
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("JBoss Reloaded does not support Descriptor deployment");      
   }
   
   @Override
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("JBoss Reloaded does not support Descriptor deployment");
   }

   private Bundle getInstalledBundle(Bundle[] bundles, String symbolicName)
   {
      for (Bundle aux : bundles)
      {
         if (symbolicName.equals(aux.getSymbolicName()))
            return aux;
      }
      return null;
   }

   private Bundle installBundle(String artifactId, boolean startBundle)
   {
      String classPath = System.getProperty("java.class.path");
      if (classPath.contains(artifactId) == false)
      {
         log.debug("Class path does not contain '" + artifactId + "'");
         return null;
      }

      String[] paths = classPath.split("" + File.pathSeparatorChar);
      for (String path : paths)
      {
         if (path.contains(artifactId))
         {
            BundleContext sysContext = frameworkInst.get().getBundleContext();
            try
            {
               Bundle bundle = sysContext.installBundle(new File(path).toURI().toString());
               if (startBundle == true)
                  bundle.start();
               
               return bundle;
            }
            catch (BundleException ex)
            {
               log.error("Cannot install bundle: " + path);
            }
         }
      }
      return null;
   }
}
