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
package org.jboss.arquillian.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.spi.ApplicationArchiveGenerator;
import org.jboss.arquillian.spi.ApplicationArchiveProcessor;
import org.jboss.arquillian.spi.AuxiliaryArchiveAppender;
import org.jboss.arquillian.spi.AuxiliaryArchiveProcessor;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.arquillian.spi.DeploymentPackager;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Responsible for calling the Packager SPIs, {@link DeploymentPackager}, {@link ApplicationArchiveGenerator},
 * {@link ApplicationArchiveProcessor}, {@link AuxiliaryArchiveAppender} and {@link AuxiliaryArchiveProcessor}. <br/>
 * The end result is the Deployment deployed to the {@link DeployableContainer} for testing.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ClientDeploymentGenerator implements DeploymentGenerator
{
   private ServiceLoader serviceLoader;
   
   public ClientDeploymentGenerator(ServiceLoader serviceLoader)
   {
      Validate.notNull(serviceLoader, "ServiceLoader must be specified");
      this.serviceLoader = serviceLoader;
   }
   
   public TestDeployment generate(TestClass testCase)
   {
      Validate.notNull(testCase, "TestCase must be specified");

      Archive<?> applicationArchive = serviceLoader.onlyOne(ApplicationArchiveGenerator.class).generateApplicationArchive(testCase);
      applyApplicationProcessors(applicationArchive, testCase);
      
      List<Archive<?>> auxiliaryArchives = loadAuxiliaryArchives();
      applyAuxiliaryProcessors(auxiliaryArchives);

      return new TestDeployment(applicationArchive, auxiliaryArchives);
   }
   
   private List<Archive<?>> loadAuxiliaryArchives() 
   {
      List<Archive<?>> archives = new ArrayList<Archive<?>>();
      Collection<AuxiliaryArchiveAppender> archiveAppenders = serviceLoader.all(AuxiliaryArchiveAppender.class);
   
      for(AuxiliaryArchiveAppender archiveAppender : archiveAppenders)
      {
         archives.add(archiveAppender.createAuxiliaryArchive());
      }
      return archives;
   }

   private void applyApplicationProcessors(Archive<?> applicationArchive, TestClass testClass)
   {
      Collection<ApplicationArchiveProcessor> processors = serviceLoader.all(ApplicationArchiveProcessor.class);
      for(ApplicationArchiveProcessor processor : processors)
      {
         processor.process(applicationArchive, testClass);
      }
   }
   
   private void applyAuxiliaryProcessors(List<Archive<?>> auxiliaryArchives)
   {
      Collection<AuxiliaryArchiveProcessor> processors = serviceLoader.all(AuxiliaryArchiveProcessor.class);
      for(AuxiliaryArchiveProcessor processor : processors)
      {
         for(Archive<?> auxiliaryArchive : auxiliaryArchives)
         {
            processor.process(auxiliaryArchive);
         }
      }
   }
}
