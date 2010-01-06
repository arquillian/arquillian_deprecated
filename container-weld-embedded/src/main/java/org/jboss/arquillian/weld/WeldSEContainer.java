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
package org.jboss.arquillian.weld;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.jboss.arquillian.protocol.local.LocalMethodExecutor;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.weld.shrinkwrap.ShrinkwrapBeanDeploymentArchive;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.context.ContextLifecycle;
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
   public final static ThreadLocal<WeldHolder> WELD_MANAGER = new ThreadLocal<WeldHolder>();
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#start()
    */
   @Override
   public void start() throws LifecycleException
   {
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#stop()
    */
   @Override
   public void stop() throws LifecycleException
   {
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#deploy(org.jboss.shrinkwrap.api.Archive)
    */
   @Override
   public ContainerMethodExecutor deploy(final Archive<?> archive)
         throws DeploymentException
   {
      final BeanDeploymentArchive beanArchive = archive.as(ShrinkwrapBeanDeploymentArchive.class);

      Deployment deployment = new Deployment() 
      {
         @Override
         public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
         {
            return Arrays.asList(beanArchive);
         }
         
         @Override
         public ServiceRegistry getServices()
         {
            return beanArchive.getServices();
         }
         
         @Override
         public BeanDeploymentArchive loadBeanDeploymentArchive(
               Class<?> beanClass)
         {
            return beanArchive;
         }
      };
      final BeanDeploymentArchive mainBeanDepArch = deployment.getBeanDeploymentArchives().iterator().next();
      
      WeldBootstrap bootstrap = new WeldBootstrap();
      bootstrap.startContainer(Environments.SE, deployment, new ConcurrentHashMapBeanStore())
                  .startInitialization()
                  .deployBeans()
                  .validateBeans()
                  .endInitialization();

      WeldManager manager = bootstrap.getManager(mainBeanDepArch);
      
      // start the session lifecycle
      manager.getServices().get(ContextLifecycle.class).restoreSession(manager.getId(), new ConcurrentHashMapBeanStore());
      
      WELD_MANAGER.set(
            new WeldHolder(
                  bootstrap, 
                  manager));

      // TODO: replace with a before/after invoke interceptor ?
      return new LocalMethodExecutor() {
         @Override
         public TestResult invoke(TestMethodExecutor testMethodExecutor)
         {
            WeldManager manager = WELD_MANAGER.get().getManager();
            String requestId = UUID.randomUUID().toString();
            try 
            {
            	// start the request lifecycle
            	manager.getServices().get(ContextLifecycle.class).beginRequest(requestId, new ConcurrentHashMapBeanStore());
            	return super.invoke(testMethodExecutor);
            } 
            finally
            {
            	// end the request lifecycle 
            	manager.getServices().get(ContextLifecycle.class).endRequest(requestId, new ConcurrentHashMapBeanStore());
            }
         }
      };
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#undeploy(org.jboss.shrinkwrap.api.Archive)
    */
   @Override
   public void undeploy(Archive<?> archive) throws DeploymentException
   {
      WeldHolder holder = WELD_MANAGER.get();
      if(holder != null) {
         WeldManager manager = holder.getManager();

         // end the session lifecycle
         manager.getServices().get(ContextLifecycle.class).endSession(manager.getId(), null);
         
         holder.getBootstrap().shutdown();
      }
      WELD_MANAGER.set(null);
   }
   
   public static class WeldHolder {
      
      private WeldBootstrap bootstrap;
      private WeldManager manager;

      public WeldHolder(WeldBootstrap bootstrap, WeldManager manager)
      {
         super();
         this.bootstrap = bootstrap;
         this.manager = manager;
      }

      public WeldBootstrap getBootstrap()
      {
         return bootstrap;
      }
      public WeldManager getManager()
      {
         return manager;
      }
   }
}