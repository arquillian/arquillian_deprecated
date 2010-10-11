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
package org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.ExpandWar;
import org.jboss.arquillian.container.tomcat.embedded_6.TomcatConfiguration;
import org.jboss.arquillian.container.tomcat.embedded_6.TomcatContainer;
import org.jboss.arquillian.container.tomcat.embedded_6.TomcatEmbedded;
import org.jboss.arquillian.protocol.servlet_3.ServletMethodExecutor;
import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.DeployableContainer;
import org.jboss.arquillian.spi.DeploymentException;
import org.jboss.arquillian.spi.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.mobicents.servlet.sip.api.ShrinkWrapSipStandardContext;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.SipStandardService;

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
public class MobicentsSipServletsContainer extends TomcatContainer
{
   private static final Logger log = Logger.getLogger(MobicentsSipServletsContainer.class.getName());   

   private static final String SIP_PROTOCOL = "sip";
   
   protected List<SipConnector> sipConnectors;   

   /**
    * Tomcat container configuration
    */
   private MobicentsSipServletsConfiguration configuration;

   public void setup(Context context, Configuration configuration)
   {
      this.configuration = configuration.getContainerConfig(MobicentsSipServletsConfiguration.class);
      setBindAddress(this.configuration.getBindAddress());
      setBindPort(this.configuration.getBindHttpPort());
      sipConnectors = getSipConnectors(this.configuration.getSipConnectors());
      setServerName(this.configuration.getServerName());
   }

   protected List<SipConnector> getSipConnectors(String sipConnectorString) {
	   List<SipConnector> connectors = new ArrayList<SipConnector>();
	   
	   StringTokenizer tokenizer = new StringTokenizer(sipConnectorString, ",");
	   while (tokenizer.hasMoreTokens()) {
		   String connectorString = tokenizer.nextToken();
		   String bindSipAddress;
		   int bindSipPort;
		   String bindSipTransport;
		   
		   int indexOfColumn = connectorString.indexOf(":");
		   int indexOfSlash = connectorString.indexOf("/");
		   if(indexOfColumn == -1) {
			   throw new IllegalArgumentException("sipConnectors configuration should be a comma separated list of <ip_address>:<port>/<transport>");
		   }
		   if(indexOfColumn == 0) {
			   bindSipAddress = getBindAddress();
		   } else {
			   bindSipAddress = connectorString.substring(0,indexOfColumn);
		   }
		   if(indexOfSlash != -1) {
			   bindSipPort = Integer.parseInt(connectorString.substring(indexOfColumn + 1, indexOfSlash));
			   bindSipTransport = connectorString.substring(indexOfSlash + 1);
		   } else {
			   bindSipPort = Integer.parseInt(connectorString.substring(indexOfColumn + 1));
			   bindSipTransport = "UDP";
		   }
		   SipConnector sipConnector = new SipConnector();
		   sipConnector.setIpAddress(bindSipAddress);
		   sipConnector.setPort(bindSipPort);
		   try {
			   sipConnector.setTransport(bindSipTransport);
		   } catch (Exception e) {}
		   connectors.add(sipConnector);
	   } 
	   
	   return connectors;
   }

   @Override
   protected TomcatEmbedded createTomcatEmbedded() throws UnknownHostException, org.apache.catalina.LifecycleException, LifecycleException
   {
	  System.setProperty("javax.servlet.sip.ar.spi.SipApplicationRouterProvider", configuration.getSipApplicationRouterProviderClassName());
	  if(MobicentsSipServletsConfiguration.MOBICENTS_DEFAULT_AR_CLASS_NAME.equals(configuration.getSipApplicationRouterProviderClassName())) {
		  System.setProperty("javax.servlet.sip.dar", Thread.currentThread().getContextClassLoader().getResource("empty-dar.properties").toString());
	  }
      // creating the tomcat embedded == service tag in server.xml
	  MobicentsSipServletsEmbedded embedded= new MobicentsSipServletsEmbedded();
	  setEmbedded(embedded);
      SipStandardService sipStandardService = new SipStandardService();
      sipStandardService.setSipApplicationDispatcherClassName(SipApplicationDispatcherImpl.class.getCanonicalName());
      sipStandardService.setCongestionControlCheckingInterval(-1);
      sipStandardService.setAdditionalParameterableHeaders("additionalParameterableHeader");
      sipStandardService.setUsePrettyEncoding(true);      
      sipStandardService.setName(getServerName());
      embedded.setService(sipStandardService);
      // TODO this needs to be a lot more robust
      String tomcatHome = configuration.getTomcatHome();
      File tomcatHomeFile = null;
      if (tomcatHome != null)
      {
         if (tomcatHome.startsWith(ENV_VAR))
         {
            String sysVar = tomcatHome.substring(ENV_VAR.length(), tomcatHome.length() - 1);
            tomcatHome = System.getProperty(sysVar);
            if (tomcatHome != null && tomcatHome.length() > 0 && new File(tomcatHome).isAbsolute())
            {
               tomcatHomeFile = new File(tomcatHome);
               log.info("Using tomcat home from environment variable: " + tomcatHome);
            }
         }
         else
         {
            tomcatHomeFile = new File(tomcatHome);
         }
      }

      if (tomcatHomeFile == null)
      {
         tomcatHomeFile = new File(System.getProperty(TMPDIR_SYS_PROP), "tomcat-embedded-6");
      }

      tomcatHomeFile.mkdirs();
      embedded.setCatalinaBase(tomcatHomeFile.getAbsolutePath());
      embedded.setCatalinaHome(tomcatHomeFile.getAbsolutePath());
     
      // creates the engine, i.e., <engine> element in server.xml
      Engine engine = embedded.createEngine();
      setEngine(engine);
      engine.setName(getServerName());
      engine.setDefaultHost(getBindAddress());
      engine.setService(sipStandardService);
      sipStandardService.setContainer(engine);
      embedded.addEngine(engine);
      
      // creates the host, i.e., <host> element in server.xml
      File appBaseFile = new File(tomcatHomeFile, getConfiguration().getAppBase());
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
      Connector connector = embedded.createConnector(InetAddress.getByName(getBindAddress()), getBindPort(), false);
      embedded.addConnector(connector);
      connector.setContainer(engine);
      
      // starts embedded Mobicents Sip Serlvets
      embedded.start();
      embedded.getService().start();
      
   // creates an sip connector, i.e., <connector> element in server.xml
      for (SipConnector sipConnector : sipConnectors) {
    	  try {
    		  (sipStandardService).addSipConnector(sipConnector);
//    			Connector sipConnector = addSipConnector(serverName, bindAddress, bindSipPort, "UDP", null);
    		} catch (Exception e) {
    			throw new LifecycleException("Couldn't create the sip connector " + sipConnector, e);
    		}
      }
      
      setWasStarted(true);
      return embedded;
   }

   protected void stopTomcatEmbedded() throws LifecycleException, org.apache.catalina.LifecycleException
   {
      getEmbedded().stop();
   }

   /**
    * Make sure an the unpacked WAR is not left behind
    * you would think Tomcat would cleanup an unpacked WAR, but it doesn't
    */
   protected void deleteUnpackedWAR(StandardContext standardContext)
   {
      File unpackDir = new File(getStandardHost().getAppBase(), standardContext.getPath().substring(1));
      if (unpackDir.exists())
      {
         ExpandWar.deleteDir(unpackDir);
      }
   }

	@Override
	public StandardContext createStandardContext(Context context, Archive<?> archive) throws DeploymentException {
		try
	      {
	         SipStandardContext sipStandardContext = archive.as(ShrinkWrapSipStandardContext.class);
	         sipStandardContext.setXmlNamespaceAware(true);
	         sipStandardContext.setManager(new SipStandardManager());
	         sipStandardContext.addLifecycleListener(new EmbeddedContextConfig());
	         sipStandardContext.setUnpackWAR(configuration.isUnpackArchive());
	         if (sipStandardContext.getUnpackWAR())
	         {
	            deleteUnpackedWAR(sipStandardContext);
	         }

	         getStandardHost().addChild(sipStandardContext);
	         context.add(SipStandardContext.class, sipStandardContext);
	         return sipStandardContext;
	      }
	      catch (Exception e)
	      {
	         throw new DeploymentException("Failed to deploy " + archive.getName(), e);
	      }	      
	}
	
	public TomcatConfiguration getConfiguration() {
		return configuration;
	}
}
