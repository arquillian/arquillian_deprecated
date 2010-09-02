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
package org.jboss.arquillian.packager.osgi;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.jar.Manifest;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeploymentPackager;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.osgi.metadata.PackageAttribute;
import org.jboss.osgi.metadata.internal.OSGiManifestMetaData;
import org.jboss.osgi.spi.util.BundleInfo;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Packager for running Arquillian against OSGi containers.
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class OSGiDeploymentPackager implements DeploymentPackager
{
   public Archive<?> generateDeployment(TestDeployment testDeployment)
   {
      Archive<?> appArchive = testDeployment.getApplicationArchive();
      assertValidateBundleArchive(appArchive);
      return appArchive;
   }

   /*
    * 
    */
   public void generateAuxiliaryArchives(Context context, TestDeployment testDeployment)
   {
      final TestClass testClass = context.get(TestClass.class);
      final Class<?> javaClass = testClass.getJavaClass();
      final Archive<?> appArchive = testDeployment.getApplicationArchive();

      // Check if the application archive already contains the test class
      String path = javaClass.getName().replace('.', '/') + ".class";
      if (appArchive.contains(path) == false)
      {
         // Generate the auxiliary archive that contains the test case
         final JavaArchive auxArchive = ShrinkWrap.create(JavaArchive.class, testClass.getSimpleName());
         auxArchive.addClass(javaClass);

         // Build the manifest
         final OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
         builder.addBundleSymbolicName(auxArchive.getName());
         builder.addBundleManifestVersion(2);
         builder.addExportPackages(javaClass);
         auxArchive.setManifest(new Asset()
         {
            public InputStream openStream()
            {
               return builder.openStream();
            }
         });

         // Generate the imported packages
         // [TODO] use bnd or another tool to do this more inteligently
         builder.addImportPackages("org.jboss.arquillian.junit");
         builder.addImportPackages("org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.asset", "org.jboss.shrinkwrap.api.spec");
         builder.addImportPackages("org.junit", "org.junit.runner", "javax.inject", "org.osgi.framework");
         builder.addImportPackages("org.jboss.osgi.spi.util", "org.jboss.osgi.testing");
         for (Annotation anno : javaClass.getDeclaredAnnotations())
         {
            addImportPackage(builder, anno.annotationType());
         }
         for (Field field : javaClass.getDeclaredFields())
         {
            Class<?> type = field.getType();
            addImportPackage(builder, type);
         }
         for (Method method : javaClass.getDeclaredMethods())
         {
            Class<?> returnType = method.getReturnType();
            if (returnType != Void.TYPE)
               addImportPackage(builder, returnType);
            for (Class<?> paramType : method.getParameterTypes())
               addImportPackage(builder, paramType);
         }
         
         // Add the exported packages from the application archive
         Manifest manifest = getBundleManifest(appArchive);
         OSGiManifestMetaData metaData = new OSGiManifestMetaData(manifest);
         List<PackageAttribute> exportPackages = metaData.getExportPackages();
         if (exportPackages != null)
         {
            for (PackageAttribute exp : exportPackages)
            {
               String packageName = exp.getPackageName();
               builder.addImportPackages(packageName);
            }
         }

         Collection<Archive<?>> auxArchives = testDeployment.getAuxiliaryArchives();
         auxArchives.add(auxArchive);
      }
   }

   private void addImportPackage(final OSGiManifestBuilder builder, final Class<?> type)
   {
      if (type.getName().startsWith("java.") == false)
         builder.addImportPackages(type);
      
      for (Annotation anno : type.getDeclaredAnnotations())
      {
         Class<?> anType = anno.annotationType();
         if (anType.getName().startsWith("java.") == false)
            builder.addImportPackages(anType);
      }
   }

   public static boolean isValidBundleArchive(Archive<?> archive)
   {
      try
      {
         assertValidateBundleArchive(archive);
         return true;
      }
      catch (Exception ex)
      {
         return false;
      }
   }

   public static void assertValidateBundleArchive(Archive<?> archive)
   {
      try
      {
         Manifest manifest = getBundleManifest(archive);
         BundleInfo.validateBundleManifest(manifest);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new IllegalArgumentException("Not a valid OSGi bundle: " + archive, ex);
      }
   }
   
   private static Manifest getBundleManifest(Archive<?> archive)
   {
      try
      {
         Node node = archive.get("META-INF/MANIFEST.MF");
         Manifest manifest = new Manifest(node.getAsset().openStream());
         return manifest;
      }
      catch (Exception ex)
      {
         return null;
      }
   }
}
