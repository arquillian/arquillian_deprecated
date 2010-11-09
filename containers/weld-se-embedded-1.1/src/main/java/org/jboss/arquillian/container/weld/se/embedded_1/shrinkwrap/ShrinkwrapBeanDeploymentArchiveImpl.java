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
package org.jboss.arquillian.container.weld.se.embedded_1.shrinkwrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.classloader.ShrinkWrapClassLoader;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.jboss.shrinkwrap.impl.base.Validate;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;

/**
 * ShrinkwrapBeanDeploymentArchiveImpl
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ShrinkwrapBeanDeploymentArchiveImpl extends AssignableBase<Archive<?>> implements ShrinkwrapBeanDeploymentArchive 
{
   private ServiceRegistry serviceRegistry = new SimpleServiceRegistry();
   
   private ShrinkWrapClassLoader classLoader;
   
   private Bootstrap bootstrap;
   
   
   public ShrinkwrapBeanDeploymentArchiveImpl(Archive<?> archive)
   {
      super(archive);
      Validate.notNull(archive, "Archive must be specified");
      
      this.classLoader = new ShrinkWrapClassLoader(archive.getClass().getClassLoader(), archive);
      
      serviceRegistry.add(ResourceLoader.class, new ResourceLoader()
      {
         public void cleanup() { }
         
         public Collection<URL> getResources(String name)
         {
            try
            {
               return Collections.list(classLoader.getResources(name));
            }
            catch (Exception e) 
            {
               throw new ResourceLoadingException(e);
            }
         }
         
         public URL getResource(String name)
         {
            return classLoader.getResource(name);
         }
         
         public Class<?> classForName(String name)
         {
            try
            {
               return classLoader.loadClass(name);
            } 
            catch (Exception e) 
            {
               throw new ResourceLoadingException(e);
            }
         }
      });
   }

   public ShrinkWrapClassLoader getClassLoader()
   {
      return classLoader;
   }
   
   public BeansXml getBeansXml()
   {
      if(bootstrap == null)
      {
         throw new RuntimeException("setBootstrap must be set. Needed to parse Beans XML");
      }
      List<URL> beansXmls = new ArrayList<URL>();
      Map<ArchivePath, Node> classes = getArchive().getContent(Filters.include(".*/beans.xml"));
      for(final Map.Entry<ArchivePath, Node> entry : classes.entrySet()) 
      {
         try 
         {
            beansXmls.add(
                  new URL(null, "archive://" + entry.getKey().get(), new URLStreamHandler() 
                  {
                     @Override
                     protected java.net.URLConnection openConnection(URL u) throws java.io.IOException 
                     {
                        return new URLConnection(u)
                        {
                           @Override
                           public void connect() throws IOException { }
                           
                           @Override
                           public InputStream getInputStream()
                                 throws IOException
                           {
                              return entry.getValue().getAsset().openStream();
                           }
                        };
                     };
                  }));
         } 
         catch (Exception e) {
            e.printStackTrace();
         }
      }
      return bootstrap.parse(beansXmls);
   }

   public Collection<String> getBeanClasses()
   {
      List<String> beanClasses = new ArrayList<String>();
      Map<ArchivePath, Node> classes = getArchive().getContent(Filters.include(".*\\.class"));

      for(Map.Entry<ArchivePath, Node> classEntry : classes.entrySet()) 
      {
         beanClasses.add(getClassName(classEntry.getKey()));
      }
      return beanClasses;
   }

   public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return Collections.emptySet();
   }

   public Collection<EjbDescriptor<?>> getEjbs()
   {
      return Collections.emptySet();
   }

   public String getId()
   {
      return getArchive().getName();
   }

   public ServiceRegistry getServices()
   {
      return serviceRegistry;
   }
 
   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.weld.se.embedded_1.shrinkwrap.ShrinkwrapBeanDeploymentArchive#setBootstrap(org.jboss.weld.bootstrap.api.Bootstrap)
    */
   public void setBootstrap(Bootstrap bootstrap)
   {
      this.bootstrap = bootstrap;
   }
   
   /*
    *  input:  /org/MyClass.class
    *  output: org.MyClass
    */
   public String getClassName(ArchivePath path)
   {
      String className = path.get();
      if(className.charAt(0) == '/')
      {
         className = className.substring(1);
      }
      className = className.replaceAll("\\.class", "");
      className = className.replaceAll("/", ".");
      return className;
   }
}