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
package org.jboss.arquillian.container.jbossas.embedded_6;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.deployment.Deployment;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.ContainerScoped;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.embedded.api.server.JBossASEmbeddedServer;
import org.jboss.embedded.api.server.JBossASEmbeddedServerFactory;

/**
 * JbossEmbeddedContainer
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JBossASEmbeddedContainer implements DeployableContainer<JBossASContainerConfiguration>
{
   @Inject @ContainerScoped
   private InstanceProducer<JBossASEmbeddedServer> serverInst;
   
   private JBossASContainerConfiguration configuration;
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getDefaultProtocol()
    */
   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 3.0");
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#getConfigurationClass()
    */
   public Class<JBossASContainerConfiguration> getConfigurationClass()
   {
      return JBossASContainerConfiguration.class;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#setup(org.jboss.arquillian.spi.client.container.ContainerConfiguration)
    */
   public void setup(JBossASContainerConfiguration configuration)
   {
      this.configuration = configuration;

      JBossASEmbeddedServer server = JBossASEmbeddedServerFactory.createServer();
      server.getConfiguration()
               .bindAddress(configuration.getBindAddress())
               .serverName(configuration.getProfileName());

      this.serverInst.set(server);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#start()
    */
   public void start() throws LifecycleException
   {
      try 
      {
         serverInst.get().start();
      }
      catch (Exception e) 
      {
         throw new LifecycleException("Could not start container", e);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#stop()
    */
   public void stop() throws LifecycleException
   {
      try 
      {
         serverInst.get().stop();
      }
      catch (Exception e) 
      {
         throw new LifecycleException("Could not stop container", e);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.arquillian.spi.client.deployment.Deployment[])
    */
   public ProtocolMetaData deploy(Deployment... deployments) throws DeploymentException
   {
      try 
      {
         for(Deployment deployment : deployments)
         {
            if(deployment.isArchiveDeployment())
            {
               serverInst.get().deploy(deployment.getArchive());
            }
            else
            {
               // TODO: create a Deploayble File ?
               // serverInst.get().deploy(deployables);
            }
         }
         
         return new ProtocolMetaData()
               .addContext(new HTTPContext(configuration.getBindAddress(), configuration.getHttpPort(), "/test"));
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not deploy to container", e);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.arquillian.spi.client.deployment.Deployment[])
    */
   public void undeploy(Deployment... deployments) throws DeploymentException
   {
      try 
      {
         for(Deployment deployment : deployments)
         {
            if(deployment.isArchiveDeployment())
            {
               serverInst.get().undeploy(deployment.getArchive());
            }
            else
            {
               // TODO: create a Deploayble File ?
               // serverInst.get().deploy(deployables);
            }
         }
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not undeploy from container", e);
      }
   }
}