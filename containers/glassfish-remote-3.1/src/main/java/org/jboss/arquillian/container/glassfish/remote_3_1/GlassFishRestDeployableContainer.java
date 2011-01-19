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
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.jboss.arquillian.protocol.servlet_3.ServletMethodExecutor;
import org.jboss.arquillian.spi.*;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.xml.sax.InputSource;

/**
 * Glassfish v3.1 remote container using REST deployment.
 *
 * @author <a href="http://community.jboss.org/people/LightGuard">Jason Porter</a>
 */
public class GlassFishRestDeployableContainer implements DeployableContainer
{
   private static final String APPLICATION = "/applications/application";

   private static final String SUCCESS = "SUCCESS";

   private String urlBase;

   private String archiveName;
   private String archivePrefix;

   @Override
   public void setup(Context context, Configuration configuration)
   {
      if (configuration == null)
      {
         throw new IllegalArgumentException("configuration must not be null");
      }

      final GlassFishRestConfiguration conf = configuration.getContainerConfig(GlassFishRestConfiguration.class);

      this.urlBase = new StringBuilder("http://").append(conf.getRemoteServerAddress()).append(":")
         .append(conf.getRemoteServerAdminPort()).append("/management/domain").toString();
   }

   @Override
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

   @Override
   public ContainerMethodExecutor deploy(Context context, Archive<?> archive) throws DeploymentException
   {
      if (archive == null)
      {
         throw new IllegalArgumentException("archive must not be null");
      }

      this.archiveName = archive.getName();

      try
      {
         // Export to a file so we can send it over the wire
         final File archiveFile = new File(new File(System.getProperty("java.io.tmpdir")), this.archiveName);
         archive.as(ZipExporter.class).exportZip(archiveFile, true);

         // Build up the POST form to send to Glassfish
         final FormDataMultiPart form = new FormDataMultiPart();
         form.getBodyParts().add(new FileDataBodyPart("id", archiveFile));
         form.field("contextroot", this.archiveName.substring(0, this.archiveName.lastIndexOf(".")), MediaType.TEXT_PLAIN_TYPE);
         archivePrefix = this.archiveName.substring(0, this.archiveName.lastIndexOf("."));
         form.field("name", archivePrefix, MediaType.TEXT_PLAIN_TYPE);
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

         return new ServletMethodExecutor(new URL(this.urlBase));
      }
      catch (IOException e)
      {
         throw new DeploymentException("Error in creating / deploying archive", e);
      }

   }

   @Override
   public void undeploy(Context context, Archive<?> archive) throws DeploymentException
   {
      final String xmlResponse = prepareClient(APPLICATION + "/" + this.archivePrefix).delete(String.class);

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

   @Override
   public void stop(Context context) throws LifecycleException
   {
      // We don't have anything to do here
   }

   private WebResource.Builder prepareClient()
   {
      return prepareClient("");
   }

   private WebResource.Builder prepareClient(String additionalResourceUrl)
   {
      final Client client = Client.create();
      return client.resource(this.urlBase + additionalResourceUrl).accept(MediaType.APPLICATION_XML_TYPE);
   }

   private boolean isCallSuccessful(String xmlResponse) throws XPathExpressionException
   {
      final XPath xpath = XPathFactory.newInstance().newXPath();

      final String exitCode = xpath.evaluate("/map/entry[@key = 'exit_code']/@value",
         new InputSource(new StringReader(xmlResponse)));

      if (exitCode == null || !SUCCESS.equals(exitCode))
      {
         return false;
      }

      return true;
   }

   private String getMessage(String xmlResponse) throws XPathExpressionException
   {
      final XPath xpath = XPathFactory.newInstance().newXPath();
      return xpath.evaluate("/map/entry[@key = 'message']/@value", new InputSource(new StringReader(xmlResponse)));
   }
}
