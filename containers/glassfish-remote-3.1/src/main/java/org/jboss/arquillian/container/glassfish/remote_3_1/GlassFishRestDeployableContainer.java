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
import java.io.StringReader;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.protocol.metadata.Servlet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Glassfish v3.1 remote container using REST deployment.
 *
 * @author <a href="http://community.jboss.org/people/LightGuard">Jason Porter</a>
 */
@SuppressWarnings({"HardcodedFileSeparator"})
public class GlassFishRestDeployableContainer implements DeployableContainer<GlassFishRestConfiguration> {
    private static final String APPLICATION = "/applications/application";

    private static final String LIST_SUB_COMPONENTS = "/applications/application/list-sub-components?id=";

    private static final String SUCCESS = "SUCCESS";

    private String adminBaseUrl;

    private String applicationBaseUrl;

    private String deploymentName;

    private GlassFishRestConfiguration configuration;

    public Class<GlassFishRestConfiguration> getConfigurationClass() {
        return GlassFishRestConfiguration.class;
    }

    public void setup(GlassFishRestConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }

        this.configuration = configuration;

        final StringBuilder adminUrlBuilder = new StringBuilder();

        if (this.configuration.isRemoteServerAdminHttps()) {
            adminUrlBuilder.append("https://");
        } else {
            adminUrlBuilder.append("http://");
        }

        adminUrlBuilder.append(this.configuration.getRemoteServerAddress()).append(":")
                .append(this.configuration.getRemoteServerAdminPort()).append("/management/domain");

        this.adminBaseUrl = adminUrlBuilder.toString();

        final StringBuilder applicationUrlBuilder = new StringBuilder();

        if (this.configuration.isRemoteServerHttps()) {
            applicationUrlBuilder.append("https://");
        } else {
            applicationUrlBuilder.append("http://");
        }

        applicationUrlBuilder.append(this.configuration.getRemoteServerAddress()).append(":")
                .append(this.configuration.getRemoteServerHttpPort()).append("/");

        this.applicationBaseUrl = applicationUrlBuilder.toString();
    }

    public void start() throws LifecycleException {
        final String xmlResponse = prepareClient().get(String.class);

        try {
            if (!isCallSuccessful(xmlResponse)) {
                throw new LifecycleException("Server is not running");
            }
        } catch (XPathExpressionException e) {
            throw new LifecycleException("Error verifying the sever is running", e);
        }
    }

    public void stop() throws LifecycleException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 3.0");
    }

    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        if (archive == null) {
            throw new IllegalArgumentException("archive must not be null");
        }

        final String archiveName = archive.getName();

        try {
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

            try {
                if (!isCallSuccessful(xmlResponse)) {
                    throw new DeploymentException(getMessage(xmlResponse));
                }
            } catch (XPathExpressionException e) {
                throw new DeploymentException("Error finding exit code or message", e);
            }

            // Call has been successful, now we need another call to get the list of servlets
            final String subComponentsResponse = prepareClient(LIST_SUB_COMPONENTS + this.deploymentName).get(String.class);

            return this.parseForProtocolMetaData(subComponentsResponse);
        } catch (XPathExpressionException e) {
            throw new DeploymentException("Error in creating / deploying archive", e);
        }
    }

    public void undeploy(Archive<?> archive) throws DeploymentException {
        final String xmlResponse = prepareClient(APPLICATION + "/" + this.deploymentName).delete(String.class);

        try {
            if (!isCallSuccessful(xmlResponse)) {
                throw new DeploymentException(getMessage(xmlResponse));
            }
        } catch (XPathExpressionException e) {
            throw new DeploymentException("Error finding exit code or message", e);
        }
    }

    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Basic REST call preparation
     *
     * @return the resource builder to execute
     */
    private WebResource.Builder prepareClient() {
        return prepareClient("");
    }

    /**
     * Basic REST call preparation, with the additional resource url appended
     *
     * @param additionalResourceUrl url portion past the base to use
     * @return the resource builder to execute
     */
    private WebResource.Builder prepareClient(String additionalResourceUrl) {
        final Client client = Client.create();
        return client.resource(this.adminBaseUrl + additionalResourceUrl).accept(MediaType.APPLICATION_XML_TYPE);
    }

    /**
     * Looks for a successful exit code given the response of the call
     *
     * @param xmlResponse XML response from the REST call
     * @return true if call was successful, false otherwise
     * @throws XPathExpressionException if the xpath query could not be executed
     */
    private boolean isCallSuccessful(String xmlResponse) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();

        final String exitCode = xpath.evaluate("/map/entry[@key = 'exit_code']/@value",
                new InputSource(new StringReader(xmlResponse)));

        return !(exitCode == null || !SUCCESS.equals(exitCode));

    }

    /**
     * Finds the message from the response.
     *
     * @param xmlResponse XML response from the REST call
     * @return true if call was successful, false otherwise
     * @throws XPathExpressionException if the xpath query could not be executed
     */
    private String getMessage(String xmlResponse) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        return xpath.evaluate("/map/entry[@key = 'message']/@value", new InputSource(new StringReader(xmlResponse)));
    }

    private ProtocolMetaData parseForProtocolMetaData(String xmlResponse) throws XPathExpressionException {
        final ProtocolMetaData protocolMetaData = new ProtocolMetaData();
        final HTTPContext httpContext = new HTTPContext(this.configuration.getRemoteServerAddress(),
                this.configuration.getRemoteServerHttpPort());

        final XPath xpath = XPathFactory.newInstance().newXPath();

        NodeList servlets = (NodeList) xpath.evaluate("/map/entry[@key = 'properties']/map/entry[@value = 'Servlet']",
                new InputSource(new StringReader(xmlResponse)), XPathConstants.NODESET);

        for (int i = 0; i < servlets.getLength(); i++) {
            httpContext.add(new Servlet(servlets.item(i).getAttributes().getNamedItem("key").getNodeValue(), this.deploymentName));
        }

        protocolMetaData.addContext(httpContext);
        return protocolMetaData;
    }
}
