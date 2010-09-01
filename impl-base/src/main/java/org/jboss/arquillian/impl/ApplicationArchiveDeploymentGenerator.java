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

import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.spi.ApplicationArchiveGenerator;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.TestDeployment;
import org.jboss.shrinkwrap.api.Archive;

/**
 * A {@link DeploymentGenerator} that will return the ApplicationArchive as is. Used for {@link RunModeType#AS_CLIENT}.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ApplicationArchiveDeploymentGenerator implements DeploymentGenerator
{
   private ServiceLoader serviceLoader;
   
   public ApplicationArchiveDeploymentGenerator(ServiceLoader serviceLoader)
   {
      this.serviceLoader = serviceLoader;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.DeploymentGenerator#generate(java.lang.Class)
    */
   public TestDeployment generate(TestClass testCase)
   {
      ApplicationArchiveGenerator generator = serviceLoader.onlyOne(ApplicationArchiveGenerator.class);
      Archive<?> appArchive = generator.generateApplicationArchive(testCase);
      return new TestDeployment(appArchive);
   }
}
