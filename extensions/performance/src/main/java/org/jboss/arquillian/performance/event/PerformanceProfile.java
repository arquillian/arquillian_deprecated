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
package org.jboss.arquillian.performance.event;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.arquillian.spi.Profile;

/**
 * PerformanceProfile
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class PerformanceProfile implements Profile
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.Profile#getClientProfile()
    */
   @SuppressWarnings("unchecked")
   @Override
   public Collection<Class<?>> getClientProfile()
   {
      return Arrays.asList(
            PerformanceTestParser.class, 
            TestPerformanceVerifier.class,
            PerformanceResultStore.class);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.Profile#getContainerProfile()
    */
   @Override
   public Collection<Class<?>> getContainerProfile()
   {
      return Arrays.asList();
   }

}
