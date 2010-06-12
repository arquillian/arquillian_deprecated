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
package org.jboss.arquillian.container.glassfish.remote30;

import org.jboss.arquillian.container.jsr88.JSR88Configuration;

/**
 * This class extends the {@link JSR88Configuration} to provide the
 * deployment URI and factory class defaults for the GlassFish container.
 *
 * @author Dan Allen
 * @author Iskandar Salim
 */
public class GlassFishJSR88Configuration extends JSR88Configuration
{
   public static final String GLASSFISH_JSR88_DEFAULT_DEPLOYMENT_URI = "deployer:Sun:AppServer::localhost:4848";
   public static final String GLASSFISH_JSR88_DEPLOYMENT_FACTORY_CLASS = "org.glassfish.deployapi.SunDeploymentFactory";

   @Override
   protected void setDefaults()
   {
      setDeploymentUri(GLASSFISH_JSR88_DEFAULT_DEPLOYMENT_URI);
      setDeploymentFactoryClass(GLASSFISH_JSR88_DEPLOYMENT_FACTORY_CLASS);
   }
}
