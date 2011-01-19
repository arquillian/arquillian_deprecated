/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

/**
 *
 * @author <a href="http://community.jboss.org/people/LightGuard">Jason Porter</a>
 */
package org.jboss.arquillian.container.glassfish.remote_3_1;

import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.testenricher.cdi.CDIInjectionEnricher;
import org.jboss.arquillian.testenricher.ejb.EJBInjectionEnricher;
import org.jboss.arquillian.testenricher.resource.ResourceInjectionEnricher;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Package the required dependencies needed by the Glassfish Container plugin
 * to run in container.
 *
 * @author <a href="http://community.jboss.org/people/aslak">Aslak Knutsen</a>
 * @author <a href="http://community.jboss.org/people/LightGuard">Jason Porter</a>
 */
public class GlassFishRestDeploymentAppender implements AuxiliaryArchiveAppender
{

   /**
    * Creates the enricher archive for the test.
    * @return Archive containing all the valid enrichers for the server.
    */
   public Archive<?> createAuxiliaryArchive()
   {
      return ShrinkWrap.create(JavaArchive.class, "arquillian-glassfish-testenrichers.jar")
         .addPackages(
            true,
            EJBInjectionEnricher.class.getPackage(),
            ResourceInjectionEnricher.class.getPackage(),
            CDIInjectionEnricher.class.getPackage())
         .addAsServiceProvider(
            TestEnricher.class,
            CDIInjectionEnricher.class,
            EJBInjectionEnricher.class,
            ResourceInjectionEnricher.class);
   }
}
