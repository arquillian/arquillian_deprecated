/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.arquillian.container.openejb.embedded_3_1;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.OpenEjbConfigurationFactory;

/**
 * An extended Assembler for OpenEJB to provide access to the
 * configuration factory used by OpenEJB.
 * 
 * @author David Allen
 *
 */
public class OpenEJBAssembler extends Assembler
{
   /**
    * Provides access to the configuration factory used internally by
    * the Assembler.
    * 
    * @return the configuration factory
    */
   public OpenEjbConfigurationFactory getConfigurationFactory()
   {
      return configFactory;
   }

}
