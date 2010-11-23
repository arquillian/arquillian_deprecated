/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Dominik Dorn
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
