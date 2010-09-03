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

import java.net.URL;

import org.jboss.arquillian.spi.util.ArquillianHelper;
import org.jboss.osgi.spi.util.BundleInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * A collection of OSGi container helper menthods that can be used by Arquillian tests.
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
public final class OSGiContainer 
{
   // Hide ctor
   private OSGiContainer()
   {
   }

   public static Bundle installBundle(BundleContext context, String artifactId) throws BundleException
   {
      return installBundle(context, null, artifactId, null);
   }
   
   public static Bundle installBundle(BundleContext context, String groupId, String artifactId, String version) throws BundleException
   {
      URL artifactURL = ArquillianHelper.getArtifactURL(groupId, artifactId, version);
      if (artifactId == null)
         return null;
      
      // Verify that the artifact is a bundle
      BundleInfo info = BundleInfo.createBundleInfo(artifactURL);
      Bundle bundle = getBundle(context, info.getSymbolicName(), info.getVersion());
      if (bundle != null)
         return bundle;
      
      bundle = context.installBundle(artifactURL.toExternalForm());
      return bundle;
   }

   public static Bundle getBundle(BundleContext context, String symbolicName) throws BundleException
   {
      return getBundle(context, symbolicName, null);
   }
   
   public static Bundle getBundle(BundleContext context, String symbolicName, Version version) throws BundleException
   {
      for (Bundle bundle : context.getBundles())
      {
         boolean artefactMatch = symbolicName.equals(bundle.getSymbolicName());
         boolean versionMatch = version == null || version.equals(bundle.getVersion());
         if (artefactMatch && versionMatch)
            return bundle;
      }
      return null;
   }
}
