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
package org.jboss.arquillian.container.glassfish.embedded_3_1;

import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletRegistration;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.web.Context;
import org.glassfish.embeddable.web.VirtualServer;
import org.glassfish.embeddable.web.WebContainer;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.protocol.metadata.Servlet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

import com.sun.enterprise.web.WebModule;

/**
 * GlassfishContainer
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class GlassFishContainer implements DeployableContainer<GlassFishConfiguration>
{
   // TODO: open configuration up for bind address
   private static final String ADDRESS = "localhost";
   
   private GlassFishConfiguration configuration;
   private GlassFishRuntime glassfishRuntime;
   private GlassFish glassfish; 
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getConfigurationClass()
    */
   public Class<GlassFishConfiguration> getConfigurationClass()
   {
      return GlassFishConfiguration.class;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getDefaultProtocol()
    */
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 3.0");
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#setup(org.jboss.arquillian.spi.client.container.ContainerConfiguration)
    */
   public void setup(GlassFishConfiguration configuration)
   {
      this.configuration = configuration;
      BootstrapProperties bootstrapProps = new BootstrapProperties();
      if(configuration.getInstallRoot() != null)
      {
         bootstrapProps.setInstallRoot(configuration.getInstallRoot());
      }
      try
      {
         glassfishRuntime = GlassFishRuntime.bootstrap(bootstrapProps);
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not setup GlassFish Embedded Bootstrap", e);
      }

      GlassFishProperties serverProps = new GlassFishProperties();
      if(configuration.getInstanceRoot() != null)
      {
         File instanceRoot = new File(configuration.getInstanceRoot());
         if(!instanceRoot.exists())
         {
            instanceRoot.mkdirs();
         }
         serverProps.setInstanceRoot(configuration.getInstanceRoot());
      }
      if(configuration.getConfigurationXml() != null)
      {
         serverProps.setConfigFileURI(configuration.getConfigurationXml());
      }
      serverProps.setConfigFileReadOnly(configuration.isConfigurationReadOnly());
      serverProps.setPort("http-listener", configuration.getBindHttpPort());
      
      try
      {
         glassfish = glassfishRuntime.newGlassFish(serverProps);
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not setup GlassFish Embedded Runtime", e);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#start()
    */
   public void start() throws LifecycleException
   {
      try
      {
         glassfish.start();
      }
      catch (Exception e) 
      {
         throw new LifecycleException("Could not start GlassFish Embedded", e);
      }
      
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#stop()
    */
   public void stop() throws LifecycleException
   {
      try
      {
         glassfish.stop();
      } 
      catch (Exception e) 
      {
         throw new LifecycleException("Could not stop GlassFish Embedded", e);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.api.Archive)
    */
   public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException
   {
      String deploymentName = createDeploymentName(archive.getName());
      try
      {
         URL deploymentUrl = ShrinkWrapUtil.toURL(archive);
         
         glassfish.getDeployer().deploy(deploymentUrl.toURI(), "--name", deploymentName);
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not deploy " + archive.getName(), e);
      }
      
      try
      {
         HTTPContext httpContext = new HTTPContext(
               ADDRESS, 
               configuration.getBindHttpPort()); 
         
         findServlets(httpContext, deploymentName);
         
         return new ProtocolMetaData()
               .addContext(httpContext);
      }
      catch (GlassFishException e) 
      {
         throw new DeploymentException("Could not probe GlassFish embedded for environment", e);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.api.Archive)
    */
   public void undeploy(Archive<?> archive) throws DeploymentException
   {
      try
      {
         glassfish.getDeployer().undeploy(createDeploymentName(archive.getName()));
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not undeploy " + archive.getName(), e);
      }
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("Not implemented");
   }
   
   private String createDeploymentName(String archiveName) 
   {
      return archiveName.substring(0, archiveName.lastIndexOf("."));
   }
   
   public void findServlets(HTTPContext httpContext, String deploymentName) throws GlassFishException
   {
      WebContainer webContainer = glassfish.getService(WebContainer.class);
      for(VirtualServer server : webContainer.getVirtualServers())
      {
         for(Context context : server.getContexts())
         {
            if(context instanceof WebModule)
            {
               if(!((WebModule)context).getID().startsWith(deploymentName))
               {
                  continue;
               }
            }
            for(Map.Entry<String, ? extends ServletRegistration> servletRegistration : context.getServletRegistrations().entrySet())
            {
               httpContext.add(new Servlet(servletRegistration.getKey(), context.getContextPath()));
            }
         }
      }
   }
}