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
package org.jboss.arquillian.container.weld.se.embedded_1;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.weld.se.embedded_1.shrinkwrap.ShrinkwrapBeanDeploymentArchive;
import org.jboss.arquillian.protocol.local.LocalMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.manager.api.WeldManager;

/**
 * WeldSEContainer
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class WeldSEContainer implements DeployableContainer
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#setup(org.jboss.arquillian.spi.Context, org.jboss.arquillian.spi.Configuration)
    */
   public void setup(Context context, Configuration configuration)
   {
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#start(org.jboss.arquillian.spi.Context)
    */
   public void start(Context context) throws LifecycleException
   {
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#stop(org.jboss.arquillian.spi.Context)
    */
   public void stop(Context context) throws LifecycleException
   {
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#deploy(org.jboss.arquillian.spi.Context, org.jboss.shrinkwrap.api.Archive)
    */
   public ContainerMethodExecutor deploy(Context context, final Archive<?> archive)
         throws DeploymentException
   {
      final ShrinkwrapBeanDeploymentArchive beanArchive = archive.as(ShrinkwrapBeanDeploymentArchive.class);

      final Deployment deployment = new Deployment() 
      {
         public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
         {
            return Arrays.asList((BeanDeploymentArchive)beanArchive);
         }
         
         public ServiceRegistry getServices()
         {
            return beanArchive.getServices();
         }
         
         public BeanDeploymentArchive loadBeanDeploymentArchive(	
               Class<?> beanClass)
         {
            return beanArchive;
         }
         
         /* (non-Javadoc)
          * @see org.jboss.weld.bootstrap.spi.Deployment#getExtensions()
          */
         public Iterable<Metadata<Extension>> getExtensions()
         {
            return Collections.emptyList();
         }
      };
      
      ContextClassLoaderManager classLoaderManager = new ContextClassLoaderManager(beanArchive.getClassLoader());
      classLoaderManager.enable();

      context.add(ContextClassLoaderManager.class, classLoaderManager);
      
      WeldBootstrap bootstrap = new WeldBootstrap();
      beanArchive.setBootstrap(bootstrap);
      
      bootstrap.startContainer(Environments.SE, deployment, new ConcurrentHashMapBeanStore())
                  .startInitialization()
                  .deployBeans()
                  .validateBeans()
                  .endInitialization();

      WeldManager manager = bootstrap.getManager(beanArchive);
      
      context.add(WeldBootstrap.class, bootstrap);
      context.add(WeldManager.class, manager);
      
      return new LocalMethodExecutor();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#undeploy(org.jboss.arquillian.spi.Context, org.jboss.shrinkwrap.api.Archive)
    */
   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      WeldBootstrap bootstrap = context.get(WeldBootstrap.class);
      if(bootstrap != null)
      {
         bootstrap.shutdown();
      }
      ContextClassLoaderManager classLoaderManager = context.get(ContextClassLoaderManager.class);
      classLoaderManager.disable();
   }
}