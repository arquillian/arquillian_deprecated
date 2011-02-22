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
package org.jboss.arquillian.selenium;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.selenium.impl.ContextPathInjector;
import org.jboss.arquillian.selenium.impl.SeleniumServerConfigurator;
import org.jboss.arquillian.selenium.impl.WebTestConfigurator;
import org.jboss.arquillian.selenium.impl.WebTestCreator;
import org.jboss.arquillian.selenium.impl.WebTestDestroyer;
import org.jboss.arquillian.selenium.impl.WebTestInjector;
import org.jboss.arquillian.selenium.impl.SeleniumServerCreator;
import org.jboss.arquillian.selenium.impl.SeleniumServerDestructor;
import org.jboss.arquillian.selenium.impl.WebTestRegistrar;
import org.jboss.arquillian.spi.Profile;

/**
 * Defines a profile used to launch Arquillian extensions.
 * 
 * Currently supports only {@link RunModeType}.AS_CLIENT.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class SeleniumProfile implements Profile
{
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.spi.Profile#getClientProfile()
    */
   @SuppressWarnings("unchecked")
   public Collection<Class<?>> getClientProfile()
   {
      return Arrays.asList
         (            
                  // selenium extension
                  SeleniumServerConfigurator.class,
                  SeleniumServerCreator.class,
                  SeleniumServerDestructor.class,

                  // generic testing framework support 
                  WebTestRegistrar.class,
                  WebTestConfigurator.class,
                  WebTestCreator.class,
                  WebTestInjector.class,
                  ContextPathInjector.class,
                  WebTestDestroyer.class
         );
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.spi.Profile#getContainerProfile()
    */
   public Collection<Class<?>> getContainerProfile()
   {
      throw new IllegalArgumentException("Arquillian Selenium extension cannot be run in incontainer mode, please annotate class with @RunAs(AS_CLIENT) annotation");
   }

}
