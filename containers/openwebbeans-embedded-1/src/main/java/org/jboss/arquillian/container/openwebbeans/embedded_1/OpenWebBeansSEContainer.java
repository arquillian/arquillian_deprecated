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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.lifecycle.StandaloneLifeCycle;
import org.apache.webbeans.spi.ContainerLifecycle;
import org.jboss.arquillian.protocol.local.LocalMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;

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
   private static final Logger log = Logger.getLogger(OpenWebBeansSEContainer.class.getName());

   public final static ThreadLocal<ContainerInstanceHolder> CONTAINER_INSTANCE_HOLDER = new ThreadLocal<ContainerInstanceHolder>();
   
   public Class<OpenWebBeansConfiguration> getConfigurationClass()
   {
      return OpenWebBeansConfiguration.class;
   }
   public void setup(Configuration configuration)
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
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.api.Archive)
    */
   public ProtocolMetaData deploy(Deployment... deployments)
         throws DeploymentException
   {
      if(deployments.length > 1)
      {
         throw new IllegalArgumentException("Container only support single deployments");
      }
      if(!deployments[0].isArchiveDeployment())
      {
         throw new IllegalArgumentException("Container only support archive deployments");
      }
      Archive<?> archive = deployments[0].getArchive();
      
      ClassLoader cl = new ShrinkWrapClassLoader(archive);
      Thread.currentThread().setContextClassLoader(cl);

      final ShrinkWrapMetaDataDiscovery discovery = new ShrinkWrapMetaDataDiscovery(archive);
      ContainerLifecycle lifecycle = new StandaloneLifeCycle()
      {
         // TODO this override method will need to change to afterInitApplication(Properties) after 1.0.0-M4
         @Override
         public void init()
         {
            super.init();
            log.info("Using discovery service impl class adapted to ShrinkWrap archive : [" + discovery.getClass().getName() + "]");
            this.discoveryService = discovery;
         }
      };

      try
      {
         lifecycle.start(null);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to start standalone OpenWebBeans container", e);
      }

      BeanManager manager = lifecycle.getBeanManager();

      // start the application lifecycle
      ContextFactory.initApplicationContext(null);
      // start the session lifecycle
      HttpSession session = new MockHttpSession();
      ContextFactory.initSessionContext(session);
      
      CONTAINER_INSTANCE_HOLDER.set(new ContainerInstanceHolder(lifecycle, session, manager));

      // TODO: replace with a before/after invoke interceptor ?
      return new LocalMethodExecutor() {
         @Override
         public TestResult invoke(TestMethodExecutor testMethodExecutor)
         {
            try 
            {
            	// start the request lifecycle
               ContextFactory.initRequestContext(null);
               ContextFactory.initConversationContext(null);
               return super.invoke(testMethodExecutor);
            } 
            finally
            {
            	// end the request lifecycle
               ContextFactory.destroyConversationContext();
               ContextFactory.destroyRequestContext(null);
            }
         }
      };
   }

   /**
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.api.Archive)
    */
   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      ContainerInstanceHolder holder = CONTAINER_INSTANCE_HOLDER.get();
      if (holder != null) {
         // end the session lifecycle
         ContextFactory.destroySessionContext(holder.getSession());
         ContextFactory.destroyApplicationContext(null);
         
         holder.getLifecycle().stop(null);
         Thread.currentThread().setContextClassLoader(Thread.currentThread().getContextClassLoader().getParent());
      }
      CONTAINER_INSTANCE_HOLDER.set(null);
   }
   
   public static class ContainerInstanceHolder {
      
      private BeanManager manager;

      private ContainerLifecycle lifecycle;

      private HttpSession session;

      public ContainerInstanceHolder(ContainerLifecycle lifecycle, HttpSession session, BeanManager manager)
      {
         super();
         this.lifecycle = lifecycle;
         this.session = session;
         this.manager = manager;
      }

      public ContainerLifecycle getLifecycle()
      {
         return lifecycle;
      }

      public HttpSession getSession()
      {
         return session;
      }

      public BeanManager getManager()
      {
         return manager;
      }
   }

   private class MockHttpSession implements HttpSession
   {
      private final long creationTime;
      private final String id;
      private final Map<String, Object> attributes;
      private int maxInactiveInterval = 60000;

      public MockHttpSession()
      {
         creationTime = System.currentTimeMillis();
         id = UUID.randomUUID().toString();
         attributes = new HashMap<String, Object>();
      }

      public long getCreationTime()
      {
         return creationTime;
      }

      public String getId()
      {
         return id;
      }

      public long getLastAccessedTime()
      {
         return creationTime;
      }

      public ServletContext getServletContext()
      {
         throw new UnsupportedOperationException("Not supported.");
      }

      public void setMaxInactiveInterval(int i)
      {
         this.maxInactiveInterval = i;
      }

      public int getMaxInactiveInterval()
      {
         return maxInactiveInterval;
      }

      public HttpSessionContext getSessionContext()
      {
         throw new UnsupportedOperationException("Not supported.");
      }

      public Object getAttribute(String string)
      {
         return attributes.get(string);
      }

      public Object getValue(String string)
      {
         return getAttribute(string);
      }

      public Enumeration getAttributeNames()
      {
         final Iterator<String> nameIt = attributes.keySet().iterator();
         return new Enumeration() {

            public boolean hasMoreElements()
            {
               return nameIt.hasNext();
            }

            public Object nextElement()
            {
               return nameIt.next();
            }
         };
      }

      public String[] getValueNames()
      {
         return attributes.keySet().toArray(new String[0]);
      }

      public void setAttribute(String string, Object value)
      {
         attributes.put(string, value);
      }

      public void putValue(String string, Object value)
      {
         setAttribute(string, value);
      }

      public void removeAttribute(String string)
      {
         attributes.remove(string);
      }

      public void removeValue(String string)
      {
         removeAttribute(string);
      }

      public void invalidate()
      {
         attributes.clear();
      }

      public boolean isNew()
      {
         return true;
      }
   }
}
