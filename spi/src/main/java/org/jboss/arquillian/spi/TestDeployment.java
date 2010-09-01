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
package org.jboss.arquillian.spi;

import java.util.ArrayList;
import java.util.Collection;

import org.jboss.shrinkwrap.api.Archive;

/**
 * Value object that contains the {@link Archive}s needed for deployment. <br/>
 * 
 * With convenience methods for working / manipulating the Archives.  
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestDeployment
{
   private Archive<?> appArchive;
   private Collection<Archive<?>> auxArchives;
   
   /**
    * @param applicationArchive The user defined {@link Archive}
    */
   public TestDeployment(Archive<?> applicationArchive)
   {
      this(applicationArchive, null);
   }
   
   /**
    * @param appArchive The user defined {@link Archive}
    * @param auxArchives All extra library {@link Archive}s defined by extensions / core / frameworks. 
    */
   public TestDeployment(Archive<?> appArchive, Collection<Archive<?>> auxArchives)
   {
      if(appArchive == null)
         throw new IllegalArgumentException("ApplicationArchive must be specified");
      
      if (auxArchives == null)
         auxArchives = new ArrayList<Archive<?>>();
      
      this.appArchive = appArchive;
      this.auxArchives = auxArchives;
   }

   /**
    * Convenience method to lookup the user tagged archive for enriching.
    * @return The tagged Archive or ApplicationArchive if none are tagged
    */
   public Archive<?> getArchiveForEnrichment() 
   {
      // TODO: lookup 'tagged' archive. return applicationArchive if none found
      return appArchive;
   }

   public Archive<?> getApplicationArchive()
   {
      return appArchive;
   }
   
   public Collection<Archive<?>> getAuxiliaryArchives()
   {
      return auxArchives;
   }
}
