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
package org.jboss.arquillian.impl.domain;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.impl.MapObject;
import org.jboss.arquillian.impl.Validate;
import org.jboss.arquillian.impl.configuration.api.ContainerDef;
import org.jboss.arquillian.spi.ServiceLoader;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.test.TargetDescription;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

/**
 * ContainerManager
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ContainerRegistry
{
   private List<Container> containers;

   public ContainerRegistry()
   {
      this.containers = new ArrayList<Container>();
   }
   
   public Container create(ContainerDef definition, ServiceLoader loader)
   {
      Validate.notNull(definition, "Definition must be specified");

      try
      {
         ClassLoader containerClassLoader;
         if(definition.getDependencies().size() > 0)
         {
            final MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class).artifacts(
                  definition.getDependencies().toArray(new String[0]));
            
            URL[] resolvedURLs = MapObject.convert(resolver.resolveAsFiles());

            containerClassLoader = new FilteredURLClassLoader(resolvedURLs, "org.jboss.(arquillian|shrinkwrap)..*");
         }
         else
         {
            containerClassLoader = ContainerRegistry.class.getClassLoader();
         }
            
         return addContainer(
               new Container(
                     definition.getContainerName(), 
                     containerClassLoader, 
                     loader.onlyOne(containerClassLoader, DeployableContainer.class),
                     definition));
      }
      catch (Exception e) 
      {
         throw new ContainerCreationException("Could not create Container " + definition.getContainerName(), e);
      }
   }

   public Container getContainer(String name)
   {
      return findMatchingContainer(name);
   }
   
   /**
    * @return the containers
    */
   public List<Container> getContainers()
   {
      return Collections.unmodifiableList(containers);
   }
   
   public Container getContainer(TargetDescription target)
   {
      Validate.notNull(target, "Target must be specified");
      if(TargetDescription.DEFAULT.equals(target))
      {
         return findDefaultContainer();
      }
      return findMatchingContainer(target.getName());
   }

   private Container addContainer(Container contianer)
   {
      containers.add(contianer);
      return contianer;
   }
   
   /**
    * @return
    */
   private Container findDefaultContainer()
   {
      if(containers.size() == 1)
      {
         return containers.get(0);
      }
      for(Container container : containers)
      {
        if(container.getContainerConfiguration().isDefault())
        {
           return container;
        }
      }
      return null;
   }

   private Container findMatchingContainer(String name)
   {
      for(Container container: containers)
      {
         if(container.getName().equals(name))
         {
            return container;
         }
      }
      return null;
   }
}