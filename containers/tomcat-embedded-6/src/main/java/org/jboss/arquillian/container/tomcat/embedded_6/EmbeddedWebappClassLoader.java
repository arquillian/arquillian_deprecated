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
package org.jboss.arquillian.container.tomcat.embedded_6;

import java.io.File;
import java.net.URL;

import org.apache.catalina.loader.ResourceEntry;
import org.apache.catalina.loader.WebappClassLoader;

/**
 * Override Tomcats WebappClassLoader to change the delegation order.
 * 
 * Deployment first, then parent/system. 
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class EmbeddedWebappClassLoader extends WebappClassLoader
{
   public EmbeddedWebappClassLoader()
   {
      super();
   }

   public EmbeddedWebappClassLoader(final ClassLoader parent)
   {
      super(parent);
   }

   @Override
   public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
   {
      Class<?> clazz = null;

      // previously been loaded ?
      clazz = findLoadedClass0(name);
      if (clazz != null)
      {
         if (resolve)
            resolveClass(clazz);
         return clazz;
      }
      
      clazz = findLoadedClass(name);
      if (clazz != null)
      {
         if (resolve)
            resolveClass(clazz);
         return clazz;
      }

      try
      {
         // do we have it?
         clazz = findClass(name);
         if (clazz != null)
         {
            if (resolve)
               resolveClass(clazz);
            return clazz;
         }
      }
      catch (ClassNotFoundException e)
      {
      }
      // check parent
      ClassLoader loader = this.parent;
      if (loader == null)
         loader = this.system;
      try
      {
         clazz = loader.loadClass(name);
         if (clazz != null)
         {
            if (resolve)
               resolveClass(clazz);
            return clazz;
         }
      }
      catch (ClassNotFoundException e)
      {
      }
      throw new ClassNotFoundException(name);
   }

   /* (non-Javadoc)
    * @see org.apache.catalina.loader.WebappClassLoader#getResource(java.lang.String)
    */
   @Override
   public URL getResource(String name)
   {
      URL url = null;
      // find locally
      url = findResource(name);
      if (url != null)
      {
         if (getAntiJARLocking())
         {
            ResourceEntry entry = (ResourceEntry) this.resourceEntries.get(name);
            try
            {
               String repository = entry.codeBase.toString();
               if ((repository.endsWith(".jar")) && (!(name.endsWith(".class"))))
               {
                  File resourceFile = new File(this.loaderDir, name);
                  url = getURI(resourceFile);
               }
            }
            catch (Exception e)
            {
            }
         }
         return url;
      }

      // check parent
      ClassLoader loader = this.parent;
      if (loader == null)
         loader = this.system;
      url = loader.getResource(name);
      if (url != null)
      {
         return url;
      }

      return null;
   }
}
