/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.container.jbossas.remote_5_0;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.protocol.metadata.Servlet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ManagementViewParser
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public final class ManagementViewParser
{
   public static ProtocolMetaData parse(String archiveName, MBeanServerConnection connection) throws Exception
   {
      ProtocolMetaData metaData = new ProtocolMetaData();
      HTTPContext httpContext = extractHTTPContext(connection);
      metaData.addContext(httpContext);

      // extract deployment content
      scanDeployment(connection, httpContext, archiveName);

      return metaData;
   }

   /**
    * @param connection
    * @param httpContext
    * @param archiveName
    */
   private static void scanDeployment(MBeanServerConnection connection, HTTPContext httpContext, String archiveName)
      throws Exception
   {
      ObjectName earExpression = new ObjectName("*:*,J2EEApplication=" + archiveName);
      Set<ObjectName> deployments = connection.queryNames(earExpression, null);
      for (ObjectName deployment : deployments)
      {
         if (deployment.getKeyProperty("j2eeType").equals("WebModule"))
         {
            scanWar(connection, httpContext, deployment);
         }
      }
   }

   /**
    * @param connection
    * @param httpContext
    * @param war
    */
   private static void scanWar(MBeanServerConnection connection, HTTPContext httpContext, ObjectName war)
      throws Exception
   {
      String descriptor = (String) connection.getAttribute(war, "deploymentDescriptor");
      List<String> servletNames = extractServletNames(descriptor);

      for (String servletName : servletNames)
      {
         Set<ObjectName> servletObjects = connection.queryNames(new ObjectName(
               "*:*,J2EEApplication=none,J2EEServer=none,j2eeType=Servlet,name=" + servletName), null);
         for (ObjectName servletObject : servletObjects)
         {
            String contextRoot = servletObject.getKeyProperty("WebModule").replaceAll(".*\\/(.*)", "$1");
            httpContext.add(new Servlet(servletName, contextRoot));
         }
      }
   }

   /**
    * @param management
    * @return
    */
   private static HTTPContext extractHTTPContext(MBeanServerConnection connection) throws Exception
   {
      Set<ObjectName> connectors = connection.queryNames(new ObjectName("jboss.web:*,type=Connector"), null);
      for(ObjectName connector : connectors)
      {
         String protocol = (String)connection.getAttribute(connector, "protocol");
         if(protocol.contains("HTTP"))
         {
            String address = ((InetAddress)connection.getAttribute(connector, "address")).getHostAddress();
            Integer port = Integer.parseInt(connector.getKeyProperty("port"));
            return new HTTPContext(address, port);
         }
      }
      return null;
   }

   private static List<String> extractServletNames(String descriptor) throws Exception
   {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(new ByteArrayInputStream(descriptor.getBytes()));

      XPathExpression xPathExpression = XPathFactory.newInstance().newXPath().compile("/web-app/servlet/servlet-name");

      NodeList nodes = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);

      List<String> servletNames = new ArrayList<String>();
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         servletNames.add(node.getTextContent());
      }
      return servletNames;
   }
}
