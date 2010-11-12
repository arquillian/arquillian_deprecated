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
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ClassScoped;
import org.jboss.arquillian.spi.core.annotation.DeploymentScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.manager.api.WeldManager;

/**
 * WeldSEContainer
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class WeldSEContainer implements DeployableContainer<WeldSEConfiguration>
{
   @Inject @DeploymentScoped
   private InstanceProducer<ContextClassLoaderManager> classLoaderManagerInst;
   
   @Inject @DeploymentScoped
   private InstanceProducer<WeldManager> weldManagerInst;

   @Inject @DeploymentScoped
   private InstanceProducer<WeldBootstrap> weldBootstrapInst;

   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Local");
   }
   
   public Class<WeldSEConfiguration> getConfigurationClass()
   {
      return WeldSEConfiguration.class;
   }
   
   public void setup(WeldSEConfiguration configuration)
   {
   }
   
   public void start() throws LifecycleException
   {
   }

   public void stop() throws LifecycleException
   {
   }
   
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("Descriptors not supported by Weld");
   }
   
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("Descriptors not supported by Weld");      
   }

   public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException
   {
      final ShrinkwrapBeanDeploymentArchive beanArchive = archive.as(ShrinkwrapBeanDeploymentArchive.class);

      final org.jboss.weld.bootstrap.spi.Deployment deployment = new org.jboss.weld.bootstrap.spi.Deployment() 
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

      classLoaderManagerInst.set(classLoaderManager);
      
      WeldBootstrap bootstrap = new WeldBootstrap();
      beanArchive.setBootstrap(bootstrap);
      
      bootstrap.startContainer(Environments.SE, deployment, new ConcurrentHashMapBeanStore())
                  .startInitialization()
                  .deployBeans()
                  .validateBeans()
                  .endInitialization();

      WeldManager manager = bootstrap.getManager(beanArchive);
      
      weldBootstrapInst.set(bootstrap);
      weldManagerInst.set(manager);
      
      return new ProtocolMetaData();
   }

   public void undeploy(Archive<?> archive) throws DeploymentException
   {
      WeldBootstrap bootstrap = weldBootstrapInst.get();
      if(bootstrap != null)
      {
         bootstrap.shutdown();
      }
      ContextClassLoaderManager classLoaderManager = classLoaderManagerInst.get();
      classLoaderManager.disable();
   }
}