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
package org.jboss.arquillian.selenium.impl;

import org.jboss.arquillian.selenium.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.selenium.event.SeleniumServerStopped;
import org.jboss.arquillian.spi.core.Event;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.suite.AfterSuite;
import org.openqa.selenium.server.SeleniumServer;

/**
 * Destructor of Selenium Server instance
 * 
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link SeleniumServerConfiguration}</li>
 * <li>{@link SeleniumServer}</li>
 * </ol>
 * 
 * <p>
 * Fires:
 * </p>
 * <ol>
 * <li>{@link SeleniumServerStopped}</li>
 * </ol>
 * 
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link AfterSuite}</li>
 * </ol>
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class SeleniumServerDestructor
{
   @Inject
   private Instance<SeleniumServerConfiguration> seleniumServerConfiguration;

   @Inject
   private Instance<SeleniumServer> seleniumServer;

   @Inject
   private Event<SeleniumServerStopped> afterStop;

   public void seleniumServerShutDown(@Observes AfterSuite event)
   {
      if (!seleniumServerConfiguration.get().isEnable())
      {
         return;
      }

      seleniumServer.get().stop();
      afterStop.fire(new SeleniumServerStopped());
   }

}
