/*
 * JBoss, Community-driven Open Source Middleware
 * Copyright 2010, JBoss by Red Hat, Inc., and individual contributors
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
package org.jboss.arquillian.container.openwebbeans.embedded_1;

import java.util.Properties;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.webbeans.lifecycle.StandaloneLifeCycle;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.DeploymentScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * An embedded Arquillian container for OpenWebBeans
 *
 * <p>This {@link DeployableContainer} implementation provides an embedded
 * container that bootstraps the OpenWebBeans JSR-299 implementation in SE (or
 * standalone) mode. It's akin to the Weld embedded container.</p>
 *
 * <p>The OpenWebBeans container is started in the deploy() method and shutdown
 * in the undeploy() method. The container is controlled using the {@link
 * StandaloneLifeCycle} from OpenWebBeans. The default discovery service
 * (metadata scanner) is replaced by a discovery service that is adapted to load
 * /META-INF/beans.xml resources and managed bean classes from a ShrinkWrap
 * archive.</p>
 *
 * <p>The current thread's context ClassLoader is also replaced with a
 * ClassLoader implementation that can discover resources in a ShrinkWrap
 * archive.</p>
 *
 * @author <a href="mailto:dan.allen@mojavelinux.com">Dan Allen</a>
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 * @see org.jboss.arquillian.weld.WeldSEContainer
 */
public class OpenWebBeansSEContainer implements DeployableContainer<OpenWebBeansConfiguration>
{
   @Inject @DeploymentScoped
   private InstanceProducer<ContainerLifecycle> lifecycleProducer;
   
   @Inject @DeploymentScoped
   private InstanceProducer<BeanManager> beanManagerProducer;
      
   /** 
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getDefaultProtocol()
    */
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Local");
   }
   
   public Class<OpenWebBeansConfiguration> getConfigurationClass()
   {
      return OpenWebBeansConfiguration.class;
   }

   public void setup(OpenWebBeansConfiguration configuration)
   {
   }
   
   /**
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#start()
    */
   public void start() throws LifecycleException
   {
   }

   /**
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#stop()
    */
   public void stop() throws LifecycleException
   {
   }

   /**
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   @Override
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("OpenWebbeans does not support deployment of Descriptors");
   }
   
   /**
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   @Override
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("OpenWebbeans does not support undeployment of Descriptors");
   }
   
   /**
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.api.Archive)
    */
   public ProtocolMetaData deploy(final Archive<?> archive)
         throws DeploymentException
   {
      /*
       * TODO: We need to reuse this ClassLoader during Before/Test/After Execution.
       * OpenWebBeans use the ClassLoader as key in looking up Singletons etc. Setting this CL during deploy
       * and not have it during Test execution makes it confused. 
       * 
      ClassLoader cl = new ShrinkWrapClassLoader(archive);
      Thread.currentThread().setContextClassLoader(cl);
       */
      final ShrinkWrapMetaDataDiscovery discovery = new ShrinkWrapMetaDataDiscovery(archive);
      ContainerLifecycle lifecycle = new StandaloneLifeCycle()
      {
         /**
          * Override so we can set out own scannerService.
          * TODO: We should change this to use the ServiceLoader via openwebbeans.properties, then do something like:
          * ((ShrinkWrapScannerService)StandardLifecycle.getScannerService()).setArchive(deployment)
          */
         @Override
         protected void afterInitApplication(Properties properties)
         {
            super.afterInitApplication(properties);
            this.scannerService = discovery;
         }
      };

      try
      {
         lifecycle.startApplication(null);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to start standalone OpenWebBeans container", e);
      }

      lifecycleProducer.set(lifecycle);
      beanManagerProducer.set(lifecycle.getBeanManager());
      
      return new ProtocolMetaData();
   }

   /**
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.api.Archive)
    */
   public void undeploy(final Archive<?> archive) throws DeploymentException
   {
      ContainerLifecycle lifecycle = lifecycleProducer.get();
      if (lifecycle != null) {
         // end the session lifecycle
         
         lifecycle.stopApplication(null);
         //Thread.currentThread().setContextClassLoader(Thread.currentThread().getContextClassLoader().getParent());
      }
   }
}
