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

import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.DeploymentScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
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
    * OpenEJB Configuration backing the Container
    */
   private ShrinkWrapConfigurationFactory config;

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
   }
   
   public ProtocolMetaData deploy(Deployment... deployments) throws DeploymentException
   {
      if(deployments == null || deployments.length > 1 || !deployments[0].isArchiveDeployment())
      {
         throw new IllegalArgumentException("Container only supports single Archive Deployments");
      }
      
      // Deploy as an archive
      final AppInfo appInfo;
      try
      {
         appInfo = config.configureApplication(deployments[0].getArchive());
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
      final ShrinkWrapConfigurationFactory config = new ShrinkWrapConfigurationFactory();
      final Assembler assembler = new Assembler();
      try
      {
         // These two objects pretty much encompass all the EJB Container
         assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
         assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
      }
      catch (final OpenEJBException e)
      {
         throw new LifecycleException("Could not configure the OpenEJB Container", e);
      }

      // Set
      this.assembler = assembler;
      this.config = new ShrinkWrapConfigurationFactory();
   }

   public void stop() throws LifecycleException
   {
      assembler.destroy();
   }

   public void undeploy(Deployment... deployments) throws DeploymentException
   {
      if(deployments == null || deployments.length > 1 || !deployments[0].isArchiveDeployment())
      {
         throw new IllegalArgumentException("Container only supports single Archive Deployments");
      }
      String deploymentName = deployments[0].getName();
      // Undeploy the archive
      try
      {
         assembler.destroyApplication(deployment.get().jarPath);
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
}
