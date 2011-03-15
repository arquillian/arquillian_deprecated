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
package org.jboss.arquillian.impl.execution;

import org.jboss.arquillian.impl.domain.Container;
import org.jboss.arquillian.impl.domain.ContainerRegistry;
import org.jboss.arquillian.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.impl.execution.event.RemoteExecutionEvent;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.spi.client.protocol.Protocol;
import org.jboss.arquillian.spi.client.protocol.ProtocolConfiguration;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.InstanceProducer;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.core.annotation.TestScoped;

/**
 * A Handler for executing the remote Test Method.<br/>
 * <br/>
 *  <b>Imports:</b><br/>
 *   {@link ProtocolMetaData}<br/>
 *   {@link DeploymentScenario}<br/>
 *   {@link ContainerRegistry}<br/>
 *   {@link ProtocolRegistry}<br/>
 *  <br/>
 *  <b>Exports:</b><br/>
 *   {@link TestResult}<br/>
 *
 * @author <a href="mailto:aknutsen@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 * @see DeployableContainer
 */
public class RemoteTestExecuter
{
   @Inject
   private Instance<DeploymentDescription> deployment;

   @Inject
   private Instance<Container> container;

   @Inject
   private Instance<ProtocolRegistry> protocolRegistry;

   @Inject
   private Instance<ProtocolMetaData> protocolMetadata;

   @Inject @TestScoped
   private InstanceProducer<TestResult> testResult;

   public void execute(@Observes RemoteExecutionEvent event) throws Exception
   {
      Container container = this.container.get();
      DeploymentDescription deployment = this.deployment.get();
      
      ProtocolRegistry protoReg = protocolRegistry.get();

      // if no default marked or specific protocol defined in the registry, use the DeployableContainers defaultProtocol.
      ProtocolDefinition protocol = protoReg.getProtocol(deployment.getProtocol());
      if(protocol == null)
      {
         protocol = protoReg.getProtocol(container.getDeployableContainer().getDefaultProtocol());
      }
    
      ProtocolConfiguration protocolConfiguration;
      
      if(container.hasProtocolConfiguration(protocol.getProtocolDescription()))
      {
         protocolConfiguration = protocol.createProtocolConfiguration(
               container.getProtocolConfiguration(protocol.getProtocolDescription()).getProtocolProperties());
      } 
      else
      {
         protocolConfiguration = protocol.createProtocolConfiguration();
      }
      ContainerMethodExecutor executor = getContainerMethodExecutor(protocol, protocolConfiguration);
      testResult.set(executor.invoke(event.getExecutor()));
   }

   // TODO: cast to raw type to get away from generic issue..
   @SuppressWarnings({"unchecked", "rawtypes"})
   public ContainerMethodExecutor getContainerMethodExecutor(ProtocolDefinition protocol,
         ProtocolConfiguration protocolConfiguration)
   {
      ContainerMethodExecutor executor = ((Protocol)protocol.getProtocol()).getExecutor(protocolConfiguration, protocolMetadata.get());
      return executor;
   }
}
