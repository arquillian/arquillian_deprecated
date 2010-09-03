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
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeploymentPackager;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.osgi.spi.util.BundleInfo;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.osgi.framework.Constants;

/**
 * Packager for running Arquillian against OSGi containers.
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public class OSGiDeploymentPackager implements DeploymentPackager
{
   public Archive<?> generateDeployment(Context context, TestDeployment testDeployment)
   {
      // Arquillian generates auxiliary archives that aren't bundles
      Collection<Archive<?>> auxArchives = testDeployment.getAuxiliaryArchives();
      auxArchives.clear();
      
      Archive<?> appArchive = testDeployment.getApplicationArchive();
      
      enhanceApplicationArchive(context, (JavaArchive)appArchive);
      assertValidateBundleArchive(appArchive);
      
      return appArchive;
   }

   /*
    * Add or modify the manifest such that it exports the test class package and
    * imports all types that are used in fields, methods, annotations 
    */
   private void enhanceApplicationArchive(Context context, Archive<?> archive)
   {
      if (JavaArchive.class.isAssignableFrom(archive.getClass()) == false)
         throw new IllegalArgumentException("JavaArchive expected: " + archive);
      
      JavaArchive appArchive = JavaArchive.class.cast(archive);
      TestClass testClass = context.get(TestClass.class);
      Class<?> javaClass = testClass.getJavaClass();

      // Check if the application archive already contains the test class
      String path = javaClass.getName().replace('.', '/') + ".class";
      if (appArchive.contains(path) == false)
         appArchive.addClass(javaClass);

      final OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
      Manifest manifest = getBundleManifest(appArchive);
      if (manifest != null)
      {
         Attributes attributes = manifest.getMainAttributes();
         for (Entry<Object, Object> entry : attributes.entrySet())
         {
            String key = entry.getKey().toString();
            String value = (String)entry.getValue();
            if (key.equals("Manifest-Version"))
               continue;
            
            if (key.equals(Constants.IMPORT_PACKAGE))
            {
               String[] imports = value.split(",");
               builder.addImportPackages(imports);
               continue;
            }
            
            if (key.equals(Constants.EXPORT_PACKAGE))
            {
               String[] exports = value.split(",");
               builder.addExportPackages(exports);
               continue;
            }
            
            builder.addManifestHeader(key, value);
         }
      }
      else
      {
         builder.addBundleManifestVersion(2);
         builder.addBundleSymbolicName(appArchive.getName());
      }
      
      // Export the test class package
      builder.addExportPackages(javaClass);

      // Generate the imported packages
      // [TODO] use bnd or another tool to do this more inteligently
      builder.addImportPackages("org.jboss.arquillian.junit", "org.jboss.arquillian.osgi", "org.jboss.arquillian.spi.util");
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

      // Add the manifest to the archive
      appArchive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            return builder.openStream();
         }
      });
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

   private void assertValidateBundleArchive(Archive<?> archive)
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

   private Manifest getBundleManifest(Archive<?> archive)
   {
      try
      {
         Node node = archive.get("META-INF/MANIFEST.MF");
         if (node == null)
            return null;
         
         Manifest manifest = new Manifest(node.getAsset().openStream());
         return manifest;
      }
      catch (Exception ex)
      {
         return null;
      }
   }
}
