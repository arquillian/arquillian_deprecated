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
package org.jboss.arquillian.jboss;

import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.embedded.api.server.JBossASEmbeddedServer;
import org.jboss.embedded.core.server.JBossASEmbeddedServerImpl;
import org.jboss.shrinkwrap.api.Archive;

/**
 * JbossEmbeddedContainer
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JbossEmbeddedContainer implements DeployableContainer
{
   private JBossASEmbeddedServer server;
   
   public JbossEmbeddedContainer()
   {
      server = new JBossASEmbeddedServerImpl();
      server.getConfiguration().bindAddress("localhost");
   }

   @Override
   public void start() throws LifecycleException
   {
      try 
      {
         server.start();
      }
      catch (Exception e) 
      {
         throw new LifecycleException("Could not start container", e);
      }
   }
   
   @Override
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
   
   @Override
   public void deploy(Archive<?> archive) throws DeploymentException
   {
      try 
      {
         server.deploy(archive);
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not deploy to container", e);
      }
   }
   
   @Override
   public void undeploy(Archive<?> archive) throws DeploymentException
   {
      try 
      {
         server.undeploy(archive);
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not undeploy from container", e);
      }
   }
}
