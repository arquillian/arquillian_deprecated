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
package org.jboss.arquillian.container.tomcat.embedded;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.Embedded;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.arquillian.spi.TestMethodExecutor;
import org.jboss.arquillian.spi.TestResult;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * Arquillian {@link DeployableContainer} adaptor for a target Tomcat
 * environment; responible for lifecycle and deployment operations
 * 
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @version $Revision: $
 */
public class TomcatContainer implements DeployableContainer {

	private static final String ENV_VAR = "${env.";

	// -------------------------------------------------------------------------------------||
	// Class Members
	// ----------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||	

	private static final String SEPARATOR = "/";

	/**
	 * Logger
	 */
	private static final Logger log = Logger.getLogger(TomcatContainer.class
			.getName());

	// -------------------------------------------------------------------------------------||
	// Instance Members
	// -------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||

	/**
	 * Tomcat embedded
	 */
	private Embedded tomcatEmbedded;
	/**
	 * Engine contained within Tomcat embedded
	 */
	private Engine engine;
	
	/**
	 * Host contained in the tomcat engine
	 */
	private Host standardHost;	

	/**
	 * Tomcat configuration
	 */
	private TomcatConfiguration configuration;

	private String serverName = "tomcat";
	
	private String host = "localhost";

	private int port = 8080;

	private boolean wasStarted;

	private final List<String> failedUndeployments = new ArrayList<String>();

	// -------------------------------------------------------------------------------------||
	// Required Implementations
	// -----------------------------------------------------------||
	// -------------------------------------------------------------------------------------||

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.arquillian.spi.DeployableContainer#setup(org.jboss.arquillian
	 * .spi.Context, org.jboss.arquillian.spi.Configuration)
	 */
	public void setup(Context context, Configuration configuration) {
		this.configuration = configuration
				.getContainerConfig(TomcatConfiguration.class);
		host = this.configuration.getBindAddress();
		port = this.configuration.getHttpPort();
		if (this.configuration.getServerName() != null) {
			serverName = this.configuration.getServerName();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.arquillian.spi.DeployableContainer#start(org.jboss.arquillian
	 * .spi.Context)
	 */
	public void start(Context context) throws LifecycleException {
		try {
			startTomcatEmbedded();
		} catch (Exception e) {
			throw new LifecycleException("Bad shit happened", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.arquillian.spi.DeployableContainer#stop(org.jboss.arquillian
	 * .spi.Context)
	 */
	public void stop(Context context) throws LifecycleException {
		try {
			removeFailedUnDeployments();
		} catch (Exception e) {
			throw new LifecycleException("Could not clean up", e);
		}
		if (wasStarted) {
			try {
				stopTomcatEmbedded();
			} catch (org.apache.catalina.LifecycleException e) {
				throw new LifecycleException("An unexpected error occurred", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.arquillian.spi.DeployableContainer#deploy(org.jboss.arquillian
	 * .spi.Context, org.jboss.shrinkwrap.api.Archive)
	 */
	public ContainerMethodExecutor deploy(Context context,
			final Archive<?> archive) throws DeploymentException {
		if (archive == null) {
			throw new IllegalArgumentException("Archive must be specified");
		}
		if (tomcatEmbedded == null) {
			throw new IllegalStateException("start has not been called!");
		}
		final String deploymentName = archive.getName();

		File file = new File(deploymentName);
		archive.as(ZipExporter.class).exportZip(file, true);

		try {
			StandardContext standardContext = (StandardContext) tomcatEmbedded
					.createContext(deploymentName, file.getAbsolutePath());
			StandardManager manager = new StandardManager();		
			standardContext.setManager(manager);
			standardContext.setParent(standardHost);
         if (configuration.getTomcatWorkDir() != null)
         {
            standardContext.setWorkDir(configuration.getTomcatWorkDir());
         }
			standardHost.addChild(standardContext);
		} catch (Exception e) {
			throw new DeploymentException("Failed to deploy " + deploymentName,
					e);
		}
		return new ContainerMethodExecutor() {
			
			public TestResult invoke(TestMethodExecutor testMethodExecutor) {
				// nothing to do here, done by the test
				return null;
			}
		};		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.arquillian.spi.DeployableContainer#undeploy(org.jboss.arquillian
	 * .spi.Context, org.jboss.shrinkwrap.api.Archive)
	 */
	public void undeploy(Context context, Archive<?> archive)
			throws DeploymentException {
		if (archive == null) {
			throw new IllegalArgumentException("Archive must be specified");
		}		
		undeploy(archive.getName());
	}

	private void undeploy(String name) throws DeploymentException {
		Container child = standardHost.findChild(name);
		if(child != null) {
			standardHost.removeChild(child);
		}
		File file = new File(name);
		file.delete();
	}

	private void removeFailedUnDeployments() throws IOException {
		List<String> remainingDeployments = new ArrayList<String>();
		for (String name : failedUndeployments) {

			try {
				undeploy(name);

			} catch (Exception e) {
				IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			}
		}
		if (remainingDeployments.size() > 0) {
			log.severe("Failed to undeploy these artifacts: "
					+ remainingDeployments);
		}
		failedUndeployments.clear();
	}

	protected void startTomcatEmbedded() throws UnknownHostException, org.apache.catalina.LifecycleException {
		// creating the tomcat embedded == service tag in server.xml
		tomcatEmbedded = new Embedded();
		tomcatEmbedded.setName(serverName);
		String tomcatHome = configuration.getTomcatHome();
		if(tomcatHome != null) {
			if(tomcatHome.startsWith(ENV_VAR)) {
				String sysVar = tomcatHome.substring(ENV_VAR.length(), tomcatHome.length() -1);
				tomcatHome = System.getProperty(sysVar);
				System.out.println("Sys var for tomcat : " + tomcatHome);
			} 
			if(tomcatHome != null) {
				tomcatHome = new File(tomcatHome).getAbsolutePath();
				tomcatEmbedded.setCatalinaBase(tomcatHome);
				tomcatEmbedded.setCatalinaHome(tomcatHome);
			}						
		}
		// creates the engine == engine tag in server.xml
		engine = tomcatEmbedded.createEngine();
		engine.setName(serverName);
		engine.setDefaultHost(host + SEPARATOR);
		engine.setService(tomcatEmbedded);
		tomcatEmbedded.setContainer(engine);
		tomcatEmbedded.addEngine(engine);
		// creates the host == host tag in server.xml
		if(tomcatHome != null) {
			standardHost = tomcatEmbedded.createHost(host + SEPARATOR, tomcatEmbedded.getCatalinaHome() + configuration
				.getAppBase());
		} else {
			standardHost = tomcatEmbedded.createHost(host + SEPARATOR, System.getProperty("java.io.tmpdir"));
		}
		standardHost.setParent(engine);
		engine.addChild(standardHost);
		// creates an http connector == connector in server.xml
		// TODO externalize this stuff in the configuration
		Connector connector = tomcatEmbedded.createConnector(InetAddress
				.getByName(host), port, false);
		tomcatEmbedded.addConnector(connector);
		connector.setContainer(engine);
		//starts tomcat embedded
		tomcatEmbedded.init();
		tomcatEmbedded.start();
		wasStarted = true;
	}

	protected void stopTomcatEmbedded() throws LifecycleException, org.apache.catalina.LifecycleException {
		tomcatEmbedded.stop();
	}
}
