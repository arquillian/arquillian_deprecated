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
package org.jboss.arquillian.protocol.servlet_3;

import java.util.Collection;

import org.jboss.arquillian.spi.DeploymentPackager;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * ServletProtocolDeploymentPackager
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletProtocolDeploymentPackager implements DeploymentPackager
{
   private static final ArchivePath EJB_JAR_XML_JAR_PATH = ArchivePaths.create("META-INF/ejb-jar.xml");

   private static final ArchivePath EJB_JAR_XML_WAR_PATH = ArchivePaths.create("WEB-INF/ejb-jar.xml");

   private static final ArchivePath BEANS_XML_JAR_PATH = ArchivePaths.create("META-INF/beans.xml");

   private static final ArchivePath BEANS_XML_WAR_PATH = ArchivePaths.create("WEB-INF/beans.xml");

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeploymentPackager#generateDeployment(org.jboss.arquillian.spi.TestDeployment)
    */
   public Archive<?> generateDeployment(TestDeployment testDeployment)
   {
      Archive<?> protocol = new ProtocolDeploymentAppender().createAuxiliaryArchive();
      
      Archive<?> applicationArchive = testDeployment.getApplicationArchive();
      Collection<Archive<?>> auxiliaryArchives = testDeployment.getAuxiliaryArchives();
      
      if(EnterpriseArchive.class.isInstance(applicationArchive))
      {
         return handleArchive(EnterpriseArchive.class.cast(applicationArchive), auxiliaryArchives, protocol);
      } 

      if(WebArchive.class.isInstance(applicationArchive))
      {
         return handleArchive(WebArchive.class.cast(applicationArchive), auxiliaryArchives, protocol);
      } 

      if(JavaArchive.class.isInstance(applicationArchive))
      {
         return handleArchive(JavaArchive.class.cast(applicationArchive), auxiliaryArchives, protocol);
      }

      throw new IllegalArgumentException(ServletProtocolDeploymentPackager.class.getName()  + 
            " can not handle archive of type " +  applicationArchive.getClass().getName());
   }

   private Archive<?> handleArchive(WebArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, Archive<?> protocol) 
   {
      return applicationArchive
                  .addLibraries(
                        auxiliaryArchives.toArray(new Archive<?>[0]))
                  .addLibrary(protocol);
   }

   private Archive<?> handleArchive(JavaArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, Archive<?> protocol) 
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war");
      if (applicationArchive.contains(EJB_JAR_XML_JAR_PATH))
      {
         war.add(applicationArchive.get(EJB_JAR_XML_JAR_PATH).getAsset(), EJB_JAR_XML_WAR_PATH);
         applicationArchive.delete(EJB_JAR_XML_JAR_PATH);
      }
      
      if (applicationArchive.contains(BEANS_XML_JAR_PATH))
      {
         war.add(applicationArchive.get(BEANS_XML_JAR_PATH).getAsset(), BEANS_XML_WAR_PATH);
         applicationArchive.delete(BEANS_XML_JAR_PATH);
      }

      war.addLibraries(applicationArchive, protocol);
      war.addLibraries(auxiliaryArchives.toArray(new Archive[0]));
      return war;
   }

   private Archive<?> handleArchive(EnterpriseArchive applicationArchive, Collection<Archive<?>> auxiliaryArchives, Archive<?> protocol) 
   {
      if(false) // contains web archive
      {
         // find web archive and attach our self to it
      }
      else
      {
         applicationArchive.addModule(
                  ShrinkWrap.create(WebArchive.class, "test.war")
                     .addLibrary(protocol))
               .addLibraries(
                     auxiliaryArchives.toArray(new Archive<?>[0]));
      }
      return applicationArchive;
   }
}
