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
package org.jboss.arquillian.container.tomcat.embedded_6;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ExpandWar;
import org.jboss.arquillian.protocol.servlet_3.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;

/**
 * <p>Arquillian {@link DeployableContainer} implementation for an
 * Embedded Tomcat server; responsible for both lifecycle and deployment
 * operations.</p>
 *
 * <p>Please note that the context path set for the webapp must begin with
 * a forward slash. Otherwise, certain path operations within Tomcat
 * will behave inconsistently. Though it goes without saying, the host
 * name (bindAddress) cannot have a trailing slash for the same
 * reason.</p>
 * 
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @author Dan Allen
 * @version $Revision: $
 */
public abstract class TomcatContainer implements DeployableContainer
{
   private static final Logger log = Logger.getLogger(TomcatContainer.class.getName());

   protected static final String ENV_VAR = "${env.";

   protected static final String HTTP_PROTOCOL = "http";

   protected static final String TMPDIR_SYS_PROP = "java.io.tmpdir";

   /**
    * Tomcat embedded
    */
   private TomcatEmbedded embedded;

   /**
    * Engine contained within Tomcat embedded
    */
   private Engine engine;

   /**
    * Host contained in the tomcat engine
    */
   private Host standardHost;   

   private String serverName;

   private String bindAddress;

   private int bindPort;

   private boolean wasStarted;

   private final List<String> failedUndeployments = new ArrayList<String>();

   public void setup(Context context, Configuration configuration)
   {      
      bindAddress = getConfiguration().getBindAddress();
      bindPort = getConfiguration().getBindHttpPort();
      serverName = getConfiguration().getServerName();
   }

   public abstract StandardContext createStandardContext(Context context, Archive<?> archive) throws DeploymentException;
   
   public void start(Context context) throws LifecycleException
   {
      try
      {
         createTomcatEmbedded();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Bad shit happened", e);
      }
   }

   public void stop(Context context) throws LifecycleException
   {
      try
      {
         removeFailedUnDeployments();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not clean up", e);
      }
      if (wasStarted)
      {
         try
         {
            stopTomcatEmbedded();
         }
         catch (org.apache.catalina.LifecycleException e)
         {
            throw new LifecycleException("An unexpected error occurred", e);
         }
      }
   }   
   
   public ContainerMethodExecutor deploy(Context context, final Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      if (embedded == null)
      {
         throw new IllegalStateException("Embedded container is not running");
      }

      createStandardContext(context, archive);

      try
      {
         return new ServletMethodExecutor(
            new URL(
               HTTP_PROTOCOL,
               bindAddress,
               bindPort,
               "/"));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create ContainerMethodExecutor", e);
      }
   }

   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      StandardContext standardContext = context.get(StandardContext.class);
      if (standardContext != null)
      {
         standardHost.removeChild(standardContext);
         if (standardContext.getUnpackWAR())
         {
            deleteUnpackedWAR(standardContext);
         }
      }
   }

   private void undeploy(String name) throws DeploymentException
   {
      Container child = standardHost.findChild(name);
      if (child != null)
      {
         standardHost.removeChild(child);
      }
   }

   private void removeFailedUnDeployments() throws IOException
   {
      List<String> remainingDeployments = new ArrayList<String>();
      for (String name : failedUndeployments)
      {
         try
         {
            undeploy(name);
         }
         catch (Exception e)
         {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
         }
      }
      if (remainingDeployments.size() > 0)
      {
         log.severe("Failed to undeploy these artifacts: " + remainingDeployments);
      }
      failedUndeployments.clear();
   }

   protected abstract TomcatEmbedded createTomcatEmbedded() throws UnknownHostException, org.apache.catalina.LifecycleException, LifecycleException;

   protected void stopTomcatEmbedded() throws LifecycleException, org.apache.catalina.LifecycleException
   {
      embedded.stop();
   }

   /**
    * Make sure an the unpacked WAR is not left behind
    * you would think Tomcat would cleanup an unpacked WAR, but it doesn't
    */
   protected void deleteUnpackedWAR(StandardContext standardContext)
   {
      File unpackDir = new File(standardHost.getAppBase(), standardContext.getPath().substring(1));
      if (unpackDir.exists())
      {
         ExpandWar.deleteDir(unpackDir);
      }
   }
   
	/**
	 * 
	 * @return
	 */
	public Connector[] findConnectors() {
		return embedded.getService().findConnectors();
	}
	
	/**
	 * 
	 * @param connector
	 */
	public void removeConnector(Connector connector) {		
		embedded.getService().removeConnector(connector);
	}
	
	/**
	 * 
	 * @param connector
	 */
	public void addConnector(Connector connector) {		
		embedded.getService().addConnector(connector);
	}

	/**
	 * @param bindAddress the bindAddress to set
	 */
	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
	}

	/**
	 * @return the bindAddress
	 */
	public String getBindAddress() {
		return bindAddress;
	}

	/**
	 * @param bindPort the bindPort to set
	 */
	public void setBindPort(int bindPort) {
		this.bindPort = bindPort;
	}

	/**
	 * @return the bindPort
	 */
	public int getBindPort() {
		return bindPort;
	}

	/**
	 * @param wasStarted the wasStarted to set
	 */
	public void setWasStarted(boolean wasStarted) {
		this.wasStarted = wasStarted;
	}

	/**
	 * @return the wasStarted
	 */
	public boolean isWasStarted() {
		return wasStarted;
	}

	/**
	 * @param serverName the serverName to set
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * @return the configuration
	 */
	public abstract TomcatConfiguration getConfiguration();

	/**
	 * @param engine the engine to set
	 */
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	/**
	 * @return the engine
	 */
	public Engine getEngine() {
		return engine;
	}

	/**
	 * @param standardHost the standardHost to set
	 */
	public void setStandardHost(Host standardHost) {
		this.standardHost = standardHost;
	}

	/**
	 * @return the standardHost
	 */
	public Host getStandardHost() {
		return standardHost;
	}

	/**
	 * @param embedded the embedded to set
	 */
	public void setEmbedded(TomcatEmbedded embedded) {
		this.embedded = embedded;
	}

	/**
	 * @return the embedded
	 */
	public TomcatEmbedded getEmbedded() {
		return embedded;
	}
}
