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
package org.jboss.arquillian.container.osgi.embedded;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Test the embedded OSGi framework
 *
 * @author thomas.diesler@jboss.com
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class OSGiEmbeddedFrameworkTestCase
{
   @Inject
   public BundleContext context;
   
   @Inject
   public Bundle bundle;
   
   @Deployment
   public static JavaArchive createdeployment()
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar");
      archive.addClasses(SimpleActivator.class, SimpleService.class);
      archive.addClasses(OSGiEmbeddedFrameworkTestCase.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleManifestVersion(2);
            builder.addBundleActivator(SimpleActivator.class.getName());
            // [TODO] generate a separate bundle the contains the test case
            builder.addExportPackages(OSGiEmbeddedFrameworkTestCase.class);
            builder.addImportPackages("org.jboss.arquillian.junit");
            builder.addImportPackages("org.jboss.shrinkwrap.api", "org.jboss.shrinkwrap.api.asset", "org.jboss.shrinkwrap.api.spec");
            builder.addImportPackages("javax.inject", "org.junit", "org.junit.runner");
            builder.addImportPackages(BundleActivator.class);
            return builder.openStream();
         }
      });
      return archive;
   }

   //@Test
   public void testBundleContextInjection() throws Exception
   {
      assertNotNull("BundleContext injected", context);
      assertEquals("System Bundle ID", 0, context.getBundle().getBundleId());
   }

   @Test
   public void testBundleInjection() throws Exception
   {
      // Assert that the bundle is injected
      assertNotNull("Bundle injected", bundle);
      
      // Assert that the bundle is in state RESOLVED
      // Note when the test bundle contains the test case it 
      // must be resolved already when this test method is called
      assertEquals("Bundle RESOLVED", Bundle.RESOLVED, bundle.getState());
      
      // Start the bundle
      bundle.start();
      assertEquals("Bundle ACTIVE", Bundle.ACTIVE, bundle.getState());
      
      // Assert the bundle context
      BundleContext context = bundle.getBundleContext();
      assertNotNull("BundleContext available", context);
      
      // Get the service reference
      ServiceReference sref = context.getServiceReference(SimpleService.class.getName());
      assertNotNull("ServiceReference not null", sref);
      
      // Get the service for the reference
      SimpleService service = (SimpleService)context.getService(sref);
      assertNotNull("Service not null", service);
      
      // Invoke the service 
      int sum = service.sum(1, 2, 3);
      assertEquals(6, sum);
      
      // Stop the bundle
      bundle.stop();
      assertEquals("Bundle RESOLVED", Bundle.RESOLVED, bundle.getState());
   }
}
