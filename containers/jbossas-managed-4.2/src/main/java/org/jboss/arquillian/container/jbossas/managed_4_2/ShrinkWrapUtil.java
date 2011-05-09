/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.container.jbossas.managed_4_2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarOutputStream;

import org.jboss.deployment.spi.DeploymentMetaData;
import org.jboss.deployment.spi.JarUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * ShrinkWrapUtil
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
final class ShrinkWrapUtil
{
   private ShrinkWrapUtil()
   {
   }

   /**
    * Creates a tmp folder and exports the file. Returns the URL for that file location.
    * 
    * @param archive Archive to export
    * @return
    */
   public static File toFile(final Archive<?> archive)
   {
      // create a random named temp file, then delete and use it as a directory
      try
      {
         File root = File.createTempFile("arquillian", archive.getName());
         root.delete();
         root.mkdirs();

         File deployment = new File(root, archive.getName());
         deployment.deleteOnExit();
         archive.as(ZipExporter.class).exportTo(deployment, true);
         return deployment;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not export deployment to temp", e);
      }
   }

   public static URL toURL(final Descriptor descriptor)
   {
      // create a random named temp file, then delete and use it as a directory
      try
      {
         File root = File.createTempFile("arquillian", descriptor.getDescriptorName());
         root.delete();
         root.mkdirs();

         File deployment = new File(root, descriptor.getDescriptorName());
         deployment.deleteOnExit();

         FileOutputStream stream = new FileOutputStream(deployment);
         try
         {
            descriptor.exportTo(stream);
         }
         finally
         {
            try
            {
               stream.close();
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
         }

         return deployment.toURI().toURL();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not export deployment to temp", e);
      }
   }

   /**Creates a deployment plan (zip file) in the temp dir.
    * 
    * @param deploymentFile This is the file to be deployed (full path)
    * @return The file which contains the deployment plan
    * @throws IOException 
    */
   public static File createDeploymentPlan(File deployment) throws IOException
   {
      JarOutputStream jos = null;
      try
      {
         File deploymentPlan = new File(deployment.getParentFile(), "deploymentplan.zip");
         deploymentPlan.deleteOnExit();

         jos = new JarOutputStream(new FileOutputStream(deploymentPlan));

         // Setup deployment plan meta data with proprietary descriptor
         DeploymentMetaData metaData = new DeploymentMetaData(deployment.getName());

         // Add the meta data to the deployment plan
         String metaStr = metaData.toXMLString();

         JarUtils.addJarEntry(jos, DeploymentMetaData.ENTRY_NAME, new ByteArrayInputStream(metaStr.getBytes()));
         return deploymentPlan;
      }
      finally
      {
         if (jos != null)
         {
            jos.flush();
            jos.close();
         }
      }
   }

}
