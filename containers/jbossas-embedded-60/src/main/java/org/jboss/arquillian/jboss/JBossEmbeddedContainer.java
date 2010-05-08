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

import java.net.URL;

import org.jboss.arquillian.protocol.servlet.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
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
public class JBossEmbeddedContainer implements DeployableContainer
{
   private JBossASEmbeddedServer server;
   
   public JBossEmbeddedContainer()
   {
      server = new JBossASEmbeddedServerImpl();
      server.getConfiguration().bindAddress("localhost");
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#setup(org.jboss.arquillian.spi.Context, org.jboss.arquillian.spi.Configuration)
    */
   public void setup(Context context, Configuration configuration)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#start(org.jboss.arquillian.spi.Context)
    */
   public void start(Context context) throws LifecycleException
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
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#stop(org.jboss.arquillian.spi.Context)
    */
   public void stop(Context context) throws LifecycleException
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
    * @see org.jboss.arquillian.spi.DeployableContainer#deploy(org.jboss.arquillian.spi.Context, org.jboss.shrinkwrap.api.Archive)
    */
   public ContainerMethodExecutor deploy(Context context, Archive<?> archive) throws DeploymentException
   {
      try 
      {
         server.deploy(archive);
         
         return new ServletMethodExecutor(
               new URL(
                     "http",
                     server.getConfiguration().getBindAddress(),
                     8080,
                     "/")
               );
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Could not deploy to container", e);
      }
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.DeployableContainer#undeploy(org.jboss.arquillian.spi.Context, org.jboss.shrinkwrap.api.Archive)
    */
   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
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
