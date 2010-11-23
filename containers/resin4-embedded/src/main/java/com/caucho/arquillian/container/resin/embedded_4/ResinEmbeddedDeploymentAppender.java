/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package com.caucho.arquillian.container.resin.embedded_4;

import org.jboss.arquillian.spi.AuxiliaryArchiveAppender;
import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.testenricher.cdi.CDIInjectionEnricher;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

public class ResinEmbeddedDeploymentAppender implements AuxiliaryArchiveAppender
{

   public Archive<?> createAuxiliaryArchive()
   {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "arquillian-resin-testenrichers.jar")
                        .addPackages(
                              false,
                              CDIInjectionEnricher.class.getPackage()
//                              ResourceInjectionEnricher.class.getPackage())
                                      )
                        .addServiceProvider(
                              TestEnricher.class,
                              CDIInjectionEnricher.class
//                              ResourceInjectionEnricher.class);
              );
      return archive;
   }

}