/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.jboss.arquillian.impl.execution.AfterLifecycleEventExecuter;
import org.jboss.arquillian.impl.execution.BeforeLifecycleEventExecuter;
import org.jboss.arquillian.spi.Profile;

/**
 * This profile extends the CLIENT profile to include extensions
 * that are normally only for in-container use.  Since OpenEJB
 * is embedded, we need a combination of the standard extensions
 * for client and in-container.
 * 
 * @author David Allen
 *
 */
public class OpenEJBProfile implements Profile
{

   @SuppressWarnings("unchecked")
   @Override
   public Collection<Class<?>> getClientProfile()
   {
      // Add the Before/After methods executors to the client profile
      return Arrays.asList(
            AfterLifecycleEventExecuter.class,
            BeforeLifecycleEventExecuter.class
      );
   }

   @Override
   public Collection<Class<?>> getContainerProfile()
   {
      // Nothing to add to the container profile
      return Collections.EMPTY_LIST;
   }

}
