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
package org.jboss.arquillian.container.reloaded.embedded_1;

import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ContainerScoped;
import org.jboss.arquillian.spi.core.annotation.DeploymentScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.bootstrap.api.descriptor.BootstrapDescriptor;
import org.jboss.bootstrap.api.lifecycle.LifecycleState;
import org.jboss.bootstrap.api.mc.server.MCServer;
import org.jboss.bootstrap.api.mc.server.MCServerFactory;
import org.jboss.deployers.client.spi.main.MainDeployer;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.deployers.vfs.spi.client.VFSDeploymentFactory;
import org.jboss.logging.Logger;
import org.jboss.reloaded.api.ReloadedDescriptors;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.shrinkwrap.vdf.api.ShrinkWrapDeployer;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

/**
 * {@link DeployableContainer} implementation to integrate the
 * lifecycle and deployment of {@link MCServer} with
 * Arquillian.
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ReloadedContainer implements DeployableContainer<JBossReloadedConfiguration>
{
   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(ReloadedContainer.class);

   /**
    * MC bean name of the {@link ShrinkWrapDeployer}
    */
   private static final String NAME_MC_SHRINKWRAP_DEPLOYER = "ShrinkWrapDeployer";

   /**
    * Name of the system property signaling JBossXB to ignore order
    */
   private static final String NAME_SYSPROP_JBOSSXB_IGNORE_ORDER = "xb.builder.useUnorderedSequence";

   /**
    * Value to set for JBossXB ordering
    */
   private static final String VALUE_SYSPROP_JBOSSXB_IGNORE_ORDER = "true";

   /**
    * Name of the Deployment XML to install the ShrinkWrapDeployer
    */
   private static final String FILENAME_SHRINKWRAP_DEPLOYER_XML = "shrinkwrap-deployer-jboss-beans.xml";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Inject @ContainerScoped
   private InstanceProducer<ShrinkWrapDeployer> shrinkwrapDeployer;
   
   @Inject @ContainerScoped
   private InstanceProducer<MCServer> mcServer;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /** 
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getDefaultProtocol()
    */
   @Override
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Local");
   }
   
   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getConfigurationClass()
    */
   @Override
   public Class<JBossReloadedConfiguration> getConfigurationClass()
   {
      return JBossReloadedConfiguration.class;
   }
   
   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#setup(org.jboss.arquillian.spi.Context, org.jboss.arquillian.spi.Configuration)
    */
   @Override
   public void setup(final JBossReloadedConfiguration configuration)
   {
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.arquillian.spi.Context, org.jboss.shrinkwrap.api.Archive)
    */
   @Override
   public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException
   {
      // Deploy
      try
      {
         shrinkwrapDeployer.get().deploy(archive);
      }
      catch (org.jboss.deployers.spi.DeploymentException e)
      {
         // Translate the exception and wrap
         throw new DeploymentException("Encountered error while deploying " + archive.toString(), e);
      }

      // Return
      return new ProtocolMetaData();
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#start()
    */
   @Override
   public void start() throws LifecycleException
   {
      // Set up JBossXB
      AccessController.doPrivileged(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            // Must use unordered sequence else JBossXB will explode
            System.setProperty(NAME_SYSPROP_JBOSSXB_IGNORE_ORDER, VALUE_SYSPROP_JBOSSXB_IGNORE_ORDER);
            return null;
         }
      });

      // Create the Server
      final MCServer server = MCServerFactory.createServer();

      // Add the required bootstrap descriptors
      final List<BootstrapDescriptor> descriptors = server.getConfiguration().getBootstrapDescriptors();
      descriptors.add(ReloadedDescriptors.getClassLoadingDescriptor());
      descriptors.add(ReloadedDescriptors.getVdfDescriptor());

      // Start the server
      try
      {
         server.start();
      }
      catch (final Exception e)
      {
         throw new LifecycleException("Error in starting the Microcontainer server " + server, e);
      }

      // Install the ShrinkWrapDeployer
      final URL shrinkwrapDeployerJBossBeans = Thread.currentThread().getContextClassLoader()
            .getResource(FILENAME_SHRINKWRAP_DEPLOYER_XML);
      assert shrinkwrapDeployerJBossBeans != null : "ShrinkWrap Deployer beans XML not found";
      final MainDeployer mainDeployer = (MainDeployer) server.getKernel().getController()
            .getContextByClass(MainDeployer.class).getTarget();
      final VirtualFile file;
      try
      {
         file = VFS.getChild(shrinkwrapDeployerJBossBeans);
      }
      catch (final URISyntaxException e)
      {
         throw new LifecycleException("Could not create virtual file for " + shrinkwrapDeployerJBossBeans, e);
      }
      if (file == null)
      {
         throw new IllegalStateException();
      }
      final VFSDeployment deployment = VFSDeploymentFactory.getInstance().createVFSDeployment(file);
      try
      {
         mainDeployer.addDeployment(deployment);
         mainDeployer.process();
         mainDeployer.checkComplete();
      }
      catch (final org.jboss.deployers.spi.DeploymentException de)
      {
         throw new LifecycleException("Could not install ShrinkWrapDeployer", de);
      }

      // Get the ShrinkWrapDeployer
      final ShrinkWrapDeployer deployer = (ShrinkWrapDeployer) server.getKernel().getController()
            .getInstalledContext(NAME_MC_SHRINKWRAP_DEPLOYER).getTarget();

      mcServer.set(server);
      shrinkwrapDeployer.set(deployer);
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#stop()
    */
   @Override
   public void stop() throws LifecycleException
   {
      final MCServer server = mcServer.get();
      // If we've got a server
      if (server != null && server.getState().equals(LifecycleState.STARTED))
      {
         // Bring it down
         try
         {
            server.stop();
         }
         catch (final Exception e)
         {
            throw new LifecycleException("Error in stopping the Microcontainer server " + server, e);
         }
      }
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.api.Archive)
    */
   @Override
   public void undeploy(final Archive<?> archive) throws DeploymentException
   {
      // Undeploy
      try
      {
         shrinkwrapDeployer.get().undeploy(archive);
      }
      catch (org.jboss.deployers.spi.DeploymentException e)
      {
         // Translate the exception and wrap
         throw new DeploymentException("Encountered error while undeploying " + archive.toString(), e);
      }
   }
   
   @Override
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("JBoss Reloaded does not support Descriptor deployment");      
   }
   
   @Override
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("JBoss Reloaded does not support Descriptor deployment");
   }
}
