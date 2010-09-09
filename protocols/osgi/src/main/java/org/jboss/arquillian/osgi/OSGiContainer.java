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

import java.io.InputStream;

import org.jboss.shrinkwrap.api.Archive;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;

/**
 * An OSGi container that can be injected into Arquillian tests.
 *
 * @author thomas.diesler@jboss.com
 * @since 07-Sep-2010
 */
public interface OSGiContainer
{
   /**
    * Installs a bundle from the given shrinkwrap archive.
    * @throws BundleException If the bundle could not be installed
    */
   Bundle installBundle(Archive<?> archive) throws BundleException;

   /**
    * Installs a bundle from the given maven artifact id.
    * This method expects the artifact on the test client's classpath.  
    * @throws BundleException If the artifact could not be found or the bundle could not be installed
    */
   Bundle installBundle(String artifactId) throws BundleException;

   /**
    * Installs a bundle from the given maven artifact.
    * This method expects the artifact in the local maven repository.  
    * @throws BundleException If the artifact could not be found or the bundle could not be installed
    */
   Bundle installBundle(String groupId, String artifactId, String version) throws BundleException;

   /**
    * Get a bundle from the local framework instance.
    * @param symbolicName The madatory bundle symbolic name
    * @param version The optional bundle version 
    * @return The bundle or null.
    * @throws BundleException If there is a problem accessing the framework
    */
   Bundle getBundle(String symbolicName, Version version) throws BundleException;

   /**
    * Gets an archive with the given name by invoking the {@link ArchiveProvider}.
    * This method makes a callback to the client side to generate the archive.
    */
   Archive<?> getTestArchive(String name);
   
   /**
    * Gets an an input stream for an archive with the given name by invoking the {@link ArchiveProvider}.
    * This method makes a callback to the client side to generate the archive.
    */
   InputStream getTestArchiveStream(String name);
}
