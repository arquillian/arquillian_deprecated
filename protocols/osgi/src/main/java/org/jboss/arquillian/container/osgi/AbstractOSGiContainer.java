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
package org.jboss.arquillian.container.osgi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.jboss.arquillian.packager.osgi.OSGiDeploymentPackager;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.DeploymentPackager;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * An abstract OSGi container
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public abstract class AbstractOSGiContainer implements DeployableContainer
{
   // Provide logging
   private static final Logger log = Logger.getLogger(AbstractOSGiContainer.class);

   private BundleList supportBundles = new BundleList();

   public void setup(Context context, Configuration configuration)
   {
      log.debug("Setup OSGi Container");
   }

   public void start(Context context) throws LifecycleException
   {
      log.debug("Start OSGi Container");
   }

   public void stop(Context context) throws LifecycleException
   {
      uninstallBundleList(supportBundles);
      log.debug("Stop OSGi Container");
   }

   protected void installSupportBundles() 
   {
      // Install the compendium and the arquillian bundle on demand
      try
      {
         if (isBundleInstalled("osgi.cmpn") == false)
         {
            BundleHandle handle = installSupportBundle("org.osgi.compendium", false);
            supportBundles.add(handle);
         }

         if (isBundleInstalled("arquillian-osgi-bundle") == false)
         {
            BundleHandle handle = installSupportBundle("arquillian-osgi-bundle", true);
            supportBundles.add(handle);
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Cannot install support bundles", ex);
      }
   }

   public ContainerMethodExecutor deploy(Context context, final Archive<?> archive) throws DeploymentException
   {
      // Generate the auxiliary archives. This is a hack and should be done previously through public API
      OSGiDeploymentPackager packager = (OSGiDeploymentPackager)context.getServiceLoader().onlyOne(DeploymentPackager.class);
      TestDeployment deployment = context.get(TestDeployment.class);
      packager.generateAuxiliaryArchives(context, deployment);

      BundleList bundleList = new BundleList();
      context.add(BundleList.class, bundleList);

      // Install the application archive
      BundleHandle appHandle = installInternal(context, archive);
      bundleList.add(appHandle);

      // Install the auxiliary archives
      for (Archive<?> auxArchive : deployment.getAuxiliaryArchives())
      {
         if (OSGiDeploymentPackager.isValidBundleArchive(auxArchive))
         {
            BundleHandle auxHandle = installInternal(context, auxArchive);
            bundleList.add(auxHandle);
         }
      }
      
      Properties props = new Properties();
      props.put("TestBundleId", appHandle.getBundleId());
      
      return getMethodExecutor(props);
   }

   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      BundleList bundleList = context.get(BundleList.class);
      uninstallBundleList(bundleList);
   }

   private void uninstallBundleList(BundleList bundleList)
   {
      Collections.reverse(bundleList);
      for (BundleHandle handle : bundleList)
      {
         if (getBundleState(handle) != Bundle.UNINSTALLED)
            uninstallInternal(handle);
      }
      bundleList.clear();
   }

   private BundleHandle installInternal(Context context, final Archive<?> archive) throws DeploymentException
   {
      try
      {
         log.debug("Installing bundle: " + archive.getName());
         return installBundle(archive);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new DeploymentException("Cannot install bundle: " + archive.getName(), ex);
      }
   }

   private void uninstallInternal(BundleHandle handle)
   {
      try
      {
         log.debug("Uninstalling bundle: " + handle.getSymbolicName());
         uninstallBundle(handle);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         log.error("Cannot uninstall bundle: " + handle, ex);
      }
   }

   public abstract ContainerMethodExecutor getMethodExecutor(Properties props);
   
   public abstract BundleHandle installBundle(Archive<?> archive) throws BundleException, IOException;

   public abstract BundleHandle installBundle(URL bundleURL) throws BundleException, IOException;

   public abstract void uninstallBundle(BundleHandle handle) throws BundleException, IOException;

   public abstract int getBundleState(BundleHandle handle);

   public abstract void startBundle(BundleHandle handle) throws BundleException;

   public abstract void stopBundle(BundleHandle handle) throws BundleException;

   public abstract boolean isBundleInstalled(String symbolicName);

   private BundleHandle installSupportBundle(String artifactId, boolean startBundle) throws BundleException, IOException
   {
      // Check the class path for the the bundle artifact 
      String classPath = System.getProperty("java.class.path");
      if (classPath.contains(artifactId) == true)
      {
         String[] paths = classPath.split("" + File.pathSeparatorChar);
         for (String path : paths)
         {
            if (path.contains(artifactId))
            {
               BundleHandle handle = installSupportFile(new File(path), startBundle);
               return handle;
            }
         }
      }

      String archiveDir = System.getProperty("test.archive.directory");
      if (archiveDir != null)
      {
         // Check "target/test-libs" for the the bundle artifact 
         File file = new File(archiveDir + File.separator + artifactId + ".jar");
         if (file.exists())
         {
            BundleHandle handle = installSupportFile(file, startBundle);
            return handle;
         }

         // Check "target/test-libs/bundles" for the the bundle artifact 
         file = new File(archiveDir + File.separator + "bundles" + File.separator + artifactId + ".jar");
         if (file.exists())
         {
            BundleHandle handle = installSupportFile(file, startBundle);
            return handle;
         }
      }
      return null;
   }

   private BundleHandle installSupportFile(File bundleFile, boolean startBundle) throws BundleException, IOException
   {
      BundleHandle handle = installBundle(bundleFile.toURI().toURL());
      if (startBundle == true)
         startBundle(handle);
      return handle;
   }

   public static class BundleHandle
   {
      private long bundleId;
      private String symbolicName;

      public BundleHandle(long bundleId, String symbolicName)
      {
         this.bundleId = bundleId;
         this.symbolicName = symbolicName;
      }

      public long getBundleId()
      {
         return bundleId;
      }

      public String getSymbolicName()
      {
         return symbolicName;
      }

      @Override
      public String toString()
      {
         return "[" + bundleId + "]" + symbolicName;
      }
   }

   @SuppressWarnings("serial")
   static class BundleList extends ArrayList<BundleHandle>
   {
   }
}
