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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.apache.catalina.Engine;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.tomcat_6.api.ShrinkWrapStandardContext;

/**
 * <p>
 * Arquillian {@link DeployableContainer} implementation for an Embedded Tomcat
 * server; responsible for both lifecycle and deployment operations.
 * </p>
 * 
 * <p>
 * Please note that the context path set for the webapp must begin with a
 * forward slash. Otherwise, certain path operations within Tomcat will behave
 * inconsistently. Though it goes without saying, the host name (bindAddress)
 * cannot have a trailing slash for the same reason.
 * </p>
 * 
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @author Dan Allen
 * @version $Revision: $
 */
public class Tomcat6Container extends TomcatContainer {

	private static final Logger log = Logger.getLogger(Tomcat6Container.class.getName());
	/**
	 * Tomcat container configuration
	 */
	private TomcatConfiguration configuration;
	
	public void setup(Context context, Configuration configuration)
	{
		this.configuration = configuration.getContainerConfig(TomcatConfiguration.class);;
		super.setup(context, configuration);
	}
	
	@Override
	public StandardContext createStandardContext(Context context, Archive<?> archive) throws DeploymentException {
		try {
			StandardContext standardContext = archive
					.as(ShrinkWrapStandardContext.class);
			standardContext.addLifecycleListener(new EmbeddedContextConfig());
			standardContext.setUnpackWAR(configuration.isUnpackArchive());
			if (standardContext.getUnpackWAR()) {
				deleteUnpackedWAR(standardContext);
			}

			getStandardHost().addChild(standardContext);
			context.add(StandardContext.class, standardContext);
			return standardContext;
		} catch (Exception e) {
			throw new DeploymentException("Failed to deploy "
					+ archive.getName(), e);
		}
	}

	@Override
	public TomcatEmbedded createTomcatEmbedded() throws UnknownHostException, org.apache.catalina.LifecycleException, LifecycleException {
		// creating the tomcat embedded == service tag in server.xml
		TomcatEmbedded embedded = new TomcatEmbedded();
		setEmbedded(embedded);
		embedded.getService().setName(getServerName());
		// TODO this needs to be a lot more robust
		String tomcatHome = configuration.getTomcatHome();
		File tomcatHomeFile = null;
		if (tomcatHome != null) {
			if (tomcatHome.startsWith(ENV_VAR)) {
				String sysVar = tomcatHome.substring(ENV_VAR.length(),
						tomcatHome.length() - 1);
				tomcatHome = System.getProperty(sysVar);
				if (tomcatHome != null && tomcatHome.length() > 0
						&& new File(tomcatHome).isAbsolute()) {
					tomcatHomeFile = new File(tomcatHome);
					log.info("Using tomcat home from environment variable: "
							+ tomcatHome);
				}
			} else {
				tomcatHomeFile = new File(tomcatHome);
			}
		}

		if (tomcatHomeFile == null) {
			tomcatHomeFile = new File(System.getProperty(TMPDIR_SYS_PROP),
					"tomcat-embedded-6");
		}

		tomcatHomeFile.mkdirs();
		embedded.setCatalinaBase(tomcatHomeFile.getAbsolutePath());
		embedded.setCatalinaHome(tomcatHomeFile.getAbsolutePath());

		// creates the engine, i.e., <engine> element in server.xml
		Engine engine = embedded.createEngine();
		setEngine(engine);
		engine.setName(getServerName());
		engine.setDefaultHost(getBindAddress());
		engine.setService(embedded.getService());
		embedded.getService().setContainer(engine);
		embedded.addEngine(engine);

		// creates the host, i.e., <host> element in server.xml
		File appBaseFile = new File(tomcatHomeFile, configuration.getAppBase());
		appBaseFile.mkdirs();
		StandardHost host = (StandardHost) embedded.createHost(getBindAddress(), appBaseFile.getAbsolutePath());
		setStandardHost(host);
		if (configuration.getTomcatWorkDir() != null)
		{
			host.setWorkDir(configuration.getTomcatWorkDir());
		}
		host.setUnpackWARs(configuration.isUnpackArchive());
		engine.addChild(host);
      

		// creates an http connector, i.e., <connector> element in server.xml
		Connector connector = embedded.createConnector(InetAddress
				.getByName(getBindAddress()), getBindPort(), false);
		embedded.addConnector(connector);
		connector.setContainer(engine);
		
		// starts embedded tomcat
	    embedded.getService().init();
	    embedded.start();	    
	    setWasStarted(true);
	    return embedded;
	}
	
	public TomcatConfiguration getConfiguration() {
		return configuration;
	}
}
