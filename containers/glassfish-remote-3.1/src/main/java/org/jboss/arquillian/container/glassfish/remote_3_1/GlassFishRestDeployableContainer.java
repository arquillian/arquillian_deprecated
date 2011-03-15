/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

/**
 *
 * @author <a href="http://community.jboss.org/people/LightGuard">Jason Porter</a>
 */
package org.jboss.arquillian.container.glassfish.remote_3_1;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.jboss.arquillian.protocol.servlet_3.ServletMethodExecutor;
import org.jboss.arquillian.spi.*;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.xml.sax.InputSource;

/**
 * Glassfish v3.1 remote container using REST deployment.
 *
 * @author <a href="http://community.jboss.org/people/LightGuard">Jason Porter</a>
 */
@SuppressWarnings({"HardcodedFileSeparator"})
public class GlassFishRestDeployableContainer implements DeployableContainer<GlassFishRestConfiguration>
{
   private static final String APPLICATION = "/applications/application";

   private static final String SUCCESS = "SUCCESS";

   private String adminBaseUrl;

   private String applicationBaseUrl;

   private String deploymentName;

    public Class<GlassFishRestConfiguration> getConfigurationClass() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setup(GlassFishRestConfiguration configuration) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void start() throws LifecycleException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void stop() throws LifecycleException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ProtocolDescription getDefaultProtocol() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void undeploy(Archive<?> archive) throws DeploymentException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deploy(Descriptor descriptor) throws DeploymentException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void undeploy(Descriptor descriptor) throws DeploymentException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

   /**
    * Actions to configure the server / deployment.
    * @param context lifecycle content
    * @param configuration configuration for the server
    */
   public void setup(Context context, Configuration configuration)
   {
      if (configuration == null)
      {
         throw new IllegalArgumentException("configuration must not be null");
      }

      final GlassFishRestConfiguration conf = configuration.getContainerConfig(GlassFishRestConfiguration.class);

      final StringBuilder adminUrlBuilder = new StringBuilder();

      if (conf.isRemoteServerAdminHttps())
      {
         adminUrlBuilder.append("https://");
      }
      else
      {
         adminUrlBuilder.append("http://");
      }

      adminUrlBuilder.append(conf.getRemoteServerAddress()).append(":")
         .append(conf.getRemoteServerAdminPort()).append("/management/domain");

      this.adminBaseUrl = adminUrlBuilder.toString();

      final StringBuilder applicationUrlBuilder = new StringBuilder();

      if (conf.isRemoteServerHttps())
      {
         applicationUrlBuilder.append("https://");
      }
      else
      {
         applicationUrlBuilder.append("http://");
      }

      applicationUrlBuilder.append(conf.getRemoteServerAddress()).append(":")
         .append(conf.getRemoteServerHttpPort()).append("/");

      this.applicationBaseUrl = applicationUrlBuilder.toString();
   }

   /**
    * Start up / verify the server
    * @param context lifecycle context
    * @throws LifecycleException an any error
    */
   public void start(Context context) throws LifecycleException
   {
      final String xmlResponse = prepareClient().get(String.class);

      try
      {
         if (!isCallSuccessful(xmlResponse))
         {
            throw new LifecycleException("Server is not running");
         }
      }
      catch (XPathExpressionException e)
      {
         throw new LifecycleException("Error verifying the sever is running", e);
      }

   }

   /**
    * Deploys the test archive.
    * @param context lifecycle context
    * @param archive test archive to deploy
    * @return location of the deployed archive
    * @throws DeploymentException on any exception
    */
   public ContainerMethodExecutor deploy(Context context, Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
      {
         throw new IllegalArgumentException("archive must not be null");
      }

      final String archiveName = archive.getName();

      try
      {
         // Export to a file so we can send it over the wire
         final File archiveFile = new File(new File(System.getProperty("java.io.tmpdir")), archiveName);
         archive.as(ZipExporter.class).exportZip(archiveFile, true);

         // Build up the POST form to send to Glassfish
         final FormDataMultiPart form = new FormDataMultiPart();
         form.getBodyParts().add(new FileDataBodyPart("id", archiveFile));
         form.field("contextroot", archiveName.substring(0, archiveName.lastIndexOf(".")), MediaType.TEXT_PLAIN_TYPE);
         deploymentName = archiveName.substring(0, archiveName.lastIndexOf("."));
         form.field("name", deploymentName, MediaType.TEXT_PLAIN_TYPE);
         final String xmlResponse = prepareClient(APPLICATION).type(MediaType.MULTIPART_FORM_DATA_TYPE)
            .post(String.class, form);

         try
         {
            if (!isCallSuccessful(xmlResponse))
            {
               throw new DeploymentException(getMessage(xmlResponse));
            }
         }
         catch (XPathExpressionException e)
         {
            throw new DeploymentException("Error finding exit code or message", e);
         }

         return new ServletMethodExecutor(new URL(this.applicationBaseUrl));
      }
      catch (IOException e)
      {
         throw new DeploymentException("Error in creating / deploying archive", e);
      }

   }

   /**
    * Undeploy the application.
    * @param context lifecycle context
    * @param archive archive to undeploy
    * @throws DeploymentException on any error
    */
   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      final String xmlResponse = prepareClient(APPLICATION + "/" + this.deploymentName).delete(String.class);

      try
      {
         if (!isCallSuccessful(xmlResponse))
         {
            throw new DeploymentException(getMessage(xmlResponse));
         }
      }
      catch (XPathExpressionException e)
      {
         throw new DeploymentException("Error finding exit code or message", e);
      }
   }

   /**
    * Stop the container.
    * @param context lifecycle context
    * @throws LifecycleException on any error
    */
   public void stop(Context context) throws LifecycleException
   {
      // We don't have anything to do here
   }

   /**
    * Basic REST call preparation
    * @return the resource builder to execute
    */
   private WebResource.Builder prepareClient()
   {
      return prepareClient("");
   }

   /**
    * Basic REST call preparation, with the additional resource url appended
    * @param additionalResourceUrl url portion past the base to use
    * @return the resource builder to execute
    */
   private WebResource.Builder prepareClient(String additionalResourceUrl)
   {
      final Client client = Client.create();
      return client.resource(this.adminBaseUrl + additionalResourceUrl).accept(MediaType.APPLICATION_XML_TYPE);
   }

   /**
    * Looks for a successful exit code given the response of the call
    * @param xmlResponse XML response from the REST call
    * @return true if call was successful, false otherwise
    * @throws XPathExpressionException if the xpath query could not be executed
    */
   private boolean isCallSuccessful(String xmlResponse) throws XPathExpressionException
   {
      final XPath xpath = XPathFactory.newInstance().newXPath();

      final String exitCode = xpath.evaluate("/map/entry[@key = 'exit_code']/@value",
         new InputSource(new StringReader(xmlResponse)));

      return !(exitCode == null || !SUCCESS.equals(exitCode));

   }

   /**
    * Finds the message from the response.
    * @param xmlResponse XML response from the REST call
    * @return true if call was successful, false otherwise
    * @throws XPathExpressionException if the xpath query could not be executed
    */
   private String getMessage(String xmlResponse) throws XPathExpressionException
   {
      final XPath xpath = XPathFactory.newInstance().newXPath();
      return xpath.evaluate("/map/entry[@key = 'message']/@value", new InputSource(new StringReader(xmlResponse)));
   }
}
