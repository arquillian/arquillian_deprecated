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
package org.jboss.arquillian.container.jetty.embedded_6_1;

import java.util.logging.Logger;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.DeploymentScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.shrinkwrap.jetty_6.api.ShrinkWrapWebAppContext;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * <p>Jetty Embedded 6.1.x container for the Arquillian project.</p>
 *
 * <p>This container only supports a WebArchive deployment. The context path of the
 * deployed application is always set to "/test", which is expected by the servlet protocol (SHRINKWRAP-196).</p>
 *
 * <p>A known issue (SHRINKWRAP-197) is that you cannot deploy two archives with the same name (i.e., test.war).
 * The ShrinkWrap Jetty extension always uses the same temporary file and doesn't delete
 * it until the JVM exists. Therefore, two successive Arquillian tests will collide trying
 * to write to the same file. The workaround is to give your archive a unique name in the
 * <code>@Deployment</code> method.</p>
 *
 * <p>Another known issue is that the container configuration process logs an exception when
 * running in-container. However, the container is still configured properly during setup.</p>
 *
 * <p>Minimum recommended Jetty version is 6.1.12, due to compatibility changes
 * with JNDI bindings (prior to 6.1.12, scoping was implemented differently).
 * Not compatible with Jetty 7 due to changes in package names.</p>
 *
 * @author Dan Allen
 * @version $Revision: $
 */
public class JettyEmbeddedContainer implements DeployableContainer<JettyEmbeddedConfiguration>
{
   public static final String HTTP_PROTOCOL = "http";

   public static final String[] JETTY_PLUS_CONFIGURATION_CLASSES =
   {
       "org.mortbay.jetty.webapp.WebInfConfiguration",
       "org.mortbay.jetty.plus.webapp.EnvConfiguration",
       //"org.mortbay.jetty.plus.webapp.Configuration",
       CompositeConfiguration.class.getName(),
       "org.mortbay.jetty.webapp.JettyWebXmlConfiguration"
   };

   private static final Logger log = Logger.getLogger(JettyEmbeddedContainer.class.getName());

   private Server server;

   private JettyEmbeddedConfiguration containerConfig;
   
   @Inject @DeploymentScoped
   private InstanceProducer<WebAppContext> webAppContextProducer;
   
   public JettyEmbeddedContainer()
   {
   }
   
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 2.5");
   }

   public Class<JettyEmbeddedConfiguration> getConfigurationClass()
   {
      return JettyEmbeddedConfiguration.class;
   }
   
   public void setup(JettyEmbeddedConfiguration containerConfig)
   {
      this.containerConfig = containerConfig;
   }
   
   public void start() throws LifecycleException
   {
      try 
      {
         server = new Server();
         Connector connector = new SelectChannelConnector();
         connector.setHost(containerConfig.getBindAddress());
         connector.setPort(containerConfig.getBindHttpPort());
         server.setConnectors(new Connector[] { connector });
         server.start();
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not start container", e);
      }
   }

   public void stop() throws LifecycleException
   {
      try 
      {
         server.stop();
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not stop container", e);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      // TODO Auto-generated method stub
      
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      // TODO Auto-generated method stub
      
   }

   public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException
   {
      try 
      {
         WebAppContext wctx = archive.as(ShrinkWrapWebAppContext.class);
         // Jetty plus is required to support in-container invocation and enrichment
         if (containerConfig.isJettyPlus())
         {
            wctx.setConfigurationClasses(JETTY_PLUS_CONFIGURATION_CLASSES);
         }
         // possible configuration parameters
         wctx.setExtractWAR(true);
         wctx.setLogUrlOnStart(true);
         
         /*
          * ARQ-242 Without this set we result in failure on loading Configuration in container.
          * ServiceLoader finds service file from AppClassLoader, tried to load JettyContainerConfiguration from AppClassLoader 
          * as a ContainerConfiguration from WebAppClassContext. ClassCastException.
          */
         wctx.setParentLoaderPriority(true);
         
         server.addHandler(wctx);
         wctx.start();
         webAppContextProducer.set(wctx);

         return new ProtocolMetaData()
            .addContext(new HTTPContext(
                  containerConfig.getBindAddress(), 
                  containerConfig.getBindHttpPort(), 
                  wctx.getContextPath()));
      } 
      catch (Exception e) 
      {
         throw new DeploymentException("Could not deploy " + archive.getName(), e);
      }
   }

   public void undeploy(final Archive<?> archive) throws DeploymentException
   {
      WebAppContext wctx = webAppContextProducer.get();
      if (wctx != null)
      {
         try
         {
            wctx.stop();
         }
         catch (Exception e)
         {
            e.printStackTrace();
            log.severe("Could not stop context " + wctx.getContextPath() + ": " + e.getMessage());
         }
         // NOTE work around a bug in Jetty where removeHandler is ignored if only one handler is set
         if (server.getHandlers() == null)
         {
            server.setHandler(null);
         }
         else
         {
            server.removeHandler(wctx);
         }
      }
   }
   
}
