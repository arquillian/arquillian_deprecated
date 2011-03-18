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
package org.jboss.arquillian.container.openejb.embedded_3_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.InitialContext;

import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.client.LocalInitialContextFactory;
import org.apache.openejb.loader.SystemInstance;
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
import org.jboss.shrinkwrap.openejb.config.ShrinkWrapConfigurationFactory;

/**
 * Arquillian {@link DeployableContainer} adaptor 
 * for a target OpenEJB environment; responible
 * for lifecycle and deployment operations
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class OpenEJBContainer implements DeployableContainer<OpenEJBConfiguration>
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||


   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * OpenEJB Assembler
    */
   private Assembler assembler;
   
   /**
    * OpenEJB Configuration Factory for the Container
    */
   private ShrinkWrapConfigurationFactory config;

   /**
    * OpenEJB Container configuration for Arquillian
    */
   private OpenEJBConfiguration containerConfig;

   /**
    * The deployment
    */
   @Inject @DeploymentScoped
   private InstanceProducer<AppInfo> deployment;
   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   public ProtocolDescription getDefaultProtocol() 
   {
      return new ProtocolDescription("Local");
   }
   
   @Override
   public Class<OpenEJBConfiguration> getConfigurationClass()
   {
      return OpenEJBConfiguration.class;
   }
   
   @Override
   public void setup(OpenEJBConfiguration configuration)
   {
      containerConfig = configuration;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("deploy Descriptor not supported");
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("undeploy Descriptor not supported");
   }
   
   public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException
   {
      // Deploy as an archive
      final AppInfo appInfo;
      try
      {
         appInfo = config.configureApplication(archive);
         
         this.deployment.set(appInfo);
      }
      catch (final OpenEJBException e)
      {
         throw new DeploymentException("Could not configure application in OpenEJB", e);
      }
      try
      {
         assembler.createApplication(appInfo);
      }
      catch (final Exception ne)
      {
         throw new DeploymentException("Could not create the application", ne);
      }

      // Invoke locally
      return new ProtocolMetaData();
   }

   public void start() throws LifecycleException
   {
      ShrinkWrapConfigurationFactory config = null;
      OpenEJBAssembler assembler = null;
      try
      {
         // Allow the OpenEJB startup code to run services required and configured
         // by the user via external configuration resources.
         OpenEJB.init(getInitialProperties());
         assembler = (OpenEJBAssembler) SystemInstance.get().getComponent(Assembler.class);
         config = (ShrinkWrapConfigurationFactory) assembler.getConfigurationFactory();
      }
      catch (final Exception e)
      {
         throw new LifecycleException("Could not configure the OpenEJB Container", e);
      }

      // Set
      this.assembler = assembler;
      this.config = config;
   }

   public void stop() throws LifecycleException
   {
      assembler.destroy();
   }

   public void undeploy(final Archive<?> archive) throws DeploymentException
   {
      String deploymentName = archive.getName();
      // Undeploy the archive
      try
      {
         assembler.destroyApplication(deployment.get().jarPath);
         {
         }
      }
      catch (final UndeployException e)
      {
         throw new DeploymentException("Error in undeployment of " + deploymentName, e);
      }
      catch (final NoSuchApplicationException e)
      {
         throw new DeploymentException("Application was not deployed; cannot undeploy: " + deploymentName, e);
      }
   }

   // Sets up properties for OpenEJB including those from a jndi.properties file
   private Properties getInitialProperties() throws IOException
   {
      Properties properties = new Properties();
      properties.put(InitialContext.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());

      // Load properties from a jndi.properties file if it exists.
      // OpenEJB would have done this if started via the InitialContext
      if(containerConfig.getJndiProperties() != null)
      {
         File jndiPropertiesFile = new File(containerConfig.getJndiProperties());
         if(jndiPropertiesFile.exists())
         {
            InputStream jndiPropertiesStream = new FileInputStream(jndiPropertiesFile);
            if (jndiPropertiesStream != null)
            {
               properties.load(jndiPropertiesStream);
            }
         }
      }
      // configure OpenEJB to not deploy apps from the classpath
      properties.put("openejb.deployments.classpath", "false");
      // configure OpenEJB to use integration classes from Arquillian
      properties.put("openejb.configurator", ShrinkWrapConfigurationFactory.class.getName());
      properties.put("openejb.assembler", OpenEJBAssembler.class.getName());
      if (containerConfig.getOpenEjbXml() != null)
      {
         properties.put("openejb.configuration", containerConfig.getOpenEjbXml());
      }

      return properties;
   }

}
