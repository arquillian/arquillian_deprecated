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
package org.jboss.arquillian.spi.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A collection Arquillian helper methods
 *
 * @author Thomas.Diesler@jboss.com
 * @since 03-Sep-2010
 */
public final class ArquillianHelper
{
   // Hide ctor 
   private ArquillianHelper()
   {
   }


   /**
    * Get the URL for the given artifact from the 'java.class.path'.
    * 
    * If not found, fall back to the directory named by the 
    * system property 'test.archive.directory'.
    * 
    * If not found, fall back to the users local maven repository
    * 
    * @param artifactId The maven artefactId 
    * @return The URL to the artefact or null
    */
   public static URL getArtifactURL(String artifactId)
   {
      return getArtifactURL(null, artifactId, null);
   }
   
   /**
    * Get the URL for the given artifact from the 'java.class.path'.
    * 
    * If not found, fall back to the directory named by the 
    * system property 'test.archive.directory'.
    * 
    * If not found, fall back to the users local maven repository
    * 
    * @param groupId The optional maven groupId
    * @param artifactId The maven artefactId 
    * @param version The optional maven artefact version 
    * @return The URL to the artefact or null
    */
   public static URL getArtifactURL(String groupId, String artifactId, String version)
   {
      if (artifactId == null)
         throw new IllegalArgumentException("Null artifactId");

      String groupPath = null;
      if (groupId != null)
         groupPath = groupId.replace('.', File.separatorChar);

      // Check the class path for the the bundle artifact 
      String classPath = System.getProperty("java.class.path");
      if (classPath != null)
      {
         String[] paths = classPath.split("" + File.pathSeparatorChar);
         for (String path : paths)
         {
            boolean groupMatch = groupPath == null || path.contains(groupPath);
            boolean versionMatch = version == null || path.contains(version);
            if (groupMatch && path.contains(artifactId) && versionMatch)
               return failsafeURL(path);
         }
      }

      // Check "target/test-libs" for the the artifact 
      String archiveDir = System.getProperty("test.archive.directory");
      if (archiveDir != null)
      {
         File file = new File(archiveDir + File.separator + artifactId + ".jar");
         if (file.exists())
            return failsafeURL(file.getAbsolutePath());
      }

      // Check ~/.m2/repository/groupId/version/artefactId
      String userHome = System.getProperty("user.home");
      File repositoryDir = new File(userHome + File.separator + ".m2" + File.separator + "repository");
      File groupDir = new File(repositoryDir + File.separator + groupPath);
      if (groupDir.exists() && version != null)
      {
         String pathname = groupDir + File.separator + artifactId + File.separator;
         pathname += version + File.separator + artifactId + "-" + version + ".jar";
         File file = new File(pathname);
         if (file.exists())
            return failsafeURL(file.getAbsolutePath());
      }

      return null;
   }

   private static URL failsafeURL(String path)
   {
      try
      {
         return new URL(path);
      }
      catch (MalformedURLException e)
      {
         // ignore
      }

      try
      {
         File file = new File(path);
         if (file.exists())
            return file.toURI().toURL();
      }
      catch (MalformedURLException e)
      {
         // ignore
      }

      throw new IllegalArgumentException("Invalid path: " + path);
   }
}