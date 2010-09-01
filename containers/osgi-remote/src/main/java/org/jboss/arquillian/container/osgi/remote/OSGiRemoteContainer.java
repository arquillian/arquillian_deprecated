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
package org.jboss.arquillian.container.osgi.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jboss.arquillian.packager.osgi.OSGiDeploymentPackager;
import org.jboss.arquillian.protocol.jmx.JMXMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.DeploymentPackager;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.logging.Logger;
import org.jboss.osgi.spi.framework.OSGiBootstrap;
import org.jboss.osgi.spi.framework.OSGiBootstrapProvider;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;

/**
 * OSGiRemoteContainer
 *
 * @author thomas.diesler@jboss.com
 * @since 01-Sep-2010
 */
public class OSGiRemoteContainer implements DeployableContainer
{
   // Provide logging
   private static final Logger log = Logger.getLogger(OSGiRemoteContainer.class);

   private Framework framework;

   public void setup(Context context, Configuration configuration)
   {
      // [ARQ-236] Configure the Container lifecycle based on the Test events
      // bootstrapFramework(context);
   }

   public void start(Context context) throws LifecycleException
   {
      // [ARQ-236] Configure the Container lifecycle based on the Test events
      // startFramework(context);
   }

   public void stop(Context context) throws LifecycleException
   {
      // [ARQ-236] Configure the Container lifecycle based on the Test events
      // stopFramework();
   }

   private void bootstrapFramework(Context context)
   {
      log.debug("Bootstrap framework ...");
      OSGiBootstrapProvider provider = OSGiBootstrap.getBootstrapProvider();
      framework = provider.getFramework();
      context.add(Framework.class, framework);
   }

   private void startFramework(Context context) 
   {
      log.debug("Start framework: " + framework);
      try
      {
         framework.start();
         context.add(BundleContext.class, framework.getBundleContext());

         Bundle[] bundles = framework.getBundleContext().getBundles();
         if (getInstalledBundle(bundles, "osgi.cmpn") == null)
            installSupportBundle("org.osgi.compendium", false);

         if (getInstalledBundle(bundles, "arquillian-protocol-jmx-osgi-bundle") == null)
            installSupportBundle("arquillian-protocol-jmx-osgi-bundle", true);
      }
      catch (BundleException ex)
      {
         throw new IllegalStateException("Cannot start embedded OSGi Framework", ex);
      }
   }

   private void stopFramework() 
   {
      log.debug("Stop framework: " + framework);
      try
      {
         framework.stop();
         framework.waitForStop(3000);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Cannot stop embedded OSGi Framework", ex);
      }
      finally
      {
         framework = null;
      }
   }

   public ContainerMethodExecutor deploy(Context context, final Archive<?> archive) throws DeploymentException
   {
      // start the framework lazily as part of @BeforeClass
      if (framework == null)
      {
         bootstrapFramework(context);
         startFramework(context);
      }
      
      // Generate the auxiliary archives. This is a hack and should be done previously through public API
      OSGiDeploymentPackager packager = (OSGiDeploymentPackager)context.getServiceLoader().onlyOne(DeploymentPackager.class);
      TestDeployment deployment = context.get(TestDeployment.class);
      packager.generateAuxiliaryArchives(context, deployment);

      // Install the application archive
      Bundle bundle = installBundle(context, archive);
      BundleList bundleList = new BundleList(Collections.singletonList(bundle));
      context.add(BundleList.class, bundleList);

      // Install the auxiliary archives
      for (Archive<?> aux : deployment.getAuxiliaryArchives())
      {
         if (OSGiDeploymentPackager.isValidBundleArchive(aux))
         {
            Bundle auxBundle = installBundle(context, aux);
            bundleList.add(auxBundle);
         }
      }
      return new JMXMethodExecutor();
   }

   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      // Uninstall all deployed bundles
      BundleList bundleList = context.get(BundleList.class);
      for (Bundle bundle : bundleList)
      {
         try
         {
            if (bundle.getState() != Bundle.UNINSTALLED)
            {
               log.debug("Undeploy: " + bundle.getSymbolicName());
               bundle.uninstall();
            }
         }
         catch (BundleException ex)
         {
            log.error("Cannot undeploy: " + archive, ex);
         }
      }
      
      // Stop the Framework as part of @AfterClass
      bundleList.clear();
      stopFramework();
   }

   private Bundle installBundle(Context context, final Archive<?> archive) throws DeploymentException
   {
      try
      {
         // Export the bundle bytes
         ZipExporter exporter = archive.as(ZipExporter.class);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         exporter.exportZip(baos);

         log.debug("Deploy: " + archive.getName());
         BundleContext sysContext = framework.getBundleContext();
         InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
         Bundle bundle = sysContext.installBundle(archive.getName(), inputStream);
         return bundle;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new DeploymentException("Cannot deploy: " + archive, ex);
      }
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

   private Bundle installSupportBundle(String artifactId, boolean startBundle)
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
            BundleContext sysContext = framework.getBundleContext();
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
   
   @SuppressWarnings("serial")
   private static class BundleList extends ArrayList<Bundle>
   {
      public BundleList(Collection<? extends Bundle> c)
      {
         super(c);
      }
   }
}
