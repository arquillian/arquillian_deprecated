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
package org.jboss.arquillian.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An implementation of {@link ConfigurationBuilder} that loads the configuration
 * from the arquillian.xml file located in the root of the classpath. If not found,
 * it just returns an empty {@link org.jboss.arquillian.spi.Configuration} object.
 *
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author Dan Allen
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @version $Revision: $
 */
public class XmlConfigurationBuilder implements ConfigurationBuilder
{
   
   private static final Logger log = Logger.getLogger(XmlConfigurationBuilder.class.getName());
   
   /**
    * The default XML resource path.
    */
   private static final String DEFAULT_RESOURCE_PATH = "arquillian.xml";

   /**
    * The actual resourcePath
    */
   private String resourcePath;

   /**
    * Constructor. Initializes with the default resource path and service loader.
    */
   public XmlConfigurationBuilder() 
   {
       this(DEFAULT_RESOURCE_PATH);
   }

   /**
    * Constructor. Initializes with the provided resource path and the default
    * service loader.
    * @param resourcePath the path to the XML configuration file.
    */
   public XmlConfigurationBuilder(String resourcePath) 
   {
      this.resourcePath = resourcePath;
   }


   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.ConfigurationBuilder#build()
    */
   public Configuration build() throws ConfigurationException
   {      
      // the configuration object we are going to return
      Configuration configuration = new Configuration();
      
      try
      {
         Document arquillianConfiguration = loadArquillianConfiguration(resourcePath);
         if(arquillianConfiguration != null)
         {
            populateContainerConfig(configuration, arquillianConfiguration);
            populateExtensionConfig(configuration, arquillianConfiguration);            
            populateConfiguration(arquillianConfiguration, configuration);
         }
      } 
      catch (Exception e) 
      {
         throw new ConfigurationException("Could not create configuration", e);
      }
      return configuration;
   }

   private Document loadArquillianConfiguration(String resourcePath) throws Exception
   {
      InputStream inputStream = null;
      try
      {
         // load the xml configuration file
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         inputStream = classLoader.getResourceAsStream(resourcePath);
         
         if (inputStream != null) 
         {
            log.info("building configuration from XML file: " + resourcePath);
            return getDocument(inputStream);
         }
         else 
         {
            log.fine("No " + resourcePath + " file found");
         }
      } 
      finally 
      {
         if(inputStream != null)
         {
            try { inputStream.close(); } catch (Exception e) { /* NO-OP */ }
         }
      }
      return null;
   }

   /**
    * Populates a configuration by finding appropriate configuration for either
    * a container or an extension in the XML document.
    * 
    * @param configuration the Configuration object to be populated
    * @param xmlDocument the document to be parsed
    * @throws Exception
    */
   private void populateContainerConfig(Configuration configuration, Document xmlDocument) throws Exception
   {
      // load all the container nodes
      NodeList nodeList = xmlDocument.getDocumentElement().getElementsByTagNameNS("*", "container");
      for (int i=0; i < nodeList.getLength(); i++) 
      {
         Element subConfigNode = (Element) nodeList.item(i); 
         
         // retrieve the package and qualifier
         String pkg = subConfigNode.getNamespaceURI().replaceFirst("urn:arq:", "");
         String qualifier = subConfigNode.getAttribute("qualifier");
         if (qualifier != null && "".equals(qualifier)) 
         {
            qualifier = null;
         }
         
         // build properties
         Map<String, String> properties = mapNodesToProperties(subConfigNode);
         
         // add the container configuration to the configuration object
         configuration.addContainerConfig(pkg, qualifier, properties);
      }
   }
   
   /**
    * Populates a configuration by finding appropriate configuration for either
    * a container or an extension in the XML document.
    * 
    * @param <T> the type of configuration, either container or extension
    * @param xmlDocument the document to be parsed
    * @param subConfigurations the collection of available configuration for given type
    * @param localName the local part of qualified name in of the node in the document
    *        which should contain configuration
    * @throws Exception
    */
   private void populateExtensionConfig(Configuration configuration, Document xmlDocument) throws Exception
   {
      // load all the container nodes
      NodeList nodeList = xmlDocument.getDocumentElement().getElementsByTagNameNS("*", "extension");
      for (int i=0; i < nodeList.getLength(); i++) 
      {
         Element subConfigNode = (Element) nodeList.item(i); 
         
         // retrieve the package and qualifier
         String pkg = subConfigNode.getNamespaceURI().replaceFirst("urn:arq:", "");
         
         // build properties
         Map<String, String> properties = mapNodesToProperties(subConfigNode);
         
         // add the container configuration to the configuration object
         configuration.addExtensionConfig(pkg, properties);
      }
   }
   
   private void populateConfiguration(Document xmlDocument, Configuration configuration) throws Exception
   {
      // try to map all child nodes
      NodeList nodeList = xmlDocument.getDocumentElement().getElementsByTagNameNS("*", "engine");
      for (int i=0; i < nodeList.getLength(); i++) 
      {
         Node node = nodeList.item(i); 
         Map<String, String> properties = mapNodesToProperties(node);
         Configuration.populateObject(configuration, properties);
      }
   }

   /**
    * Fills the properties of the Configuration implementation object with the 
    * information from the XML fragment. 
    * @param xmlNode the XML node that represents the configuration.
    * @throws Exception if there is a problem filling the object.
    */
   private Map<String, String> mapNodesToProperties(Node xmlNode) throws Exception
   {
      Validate.notNull(xmlNode, "No XML Node specified");

      
      // here we will store the properties taken from the child elements of the node
      Map<String, String> properties = new HashMap<String, String>(); 
      
      NodeList childNodes = xmlNode.getChildNodes();
      for (int i=0; i < childNodes.getLength(); i++) 
      {
         Node child = childNodes.item(i);
         
         // only process element nodes
         if (child.getNodeType() == Node.ELEMENT_NODE) 
         {
            properties.putAll(getPropertiesFromNode(child));
         }
      }

      return properties;
   }
   
   /**
    * Creates all the properties from a single Node element. The element must be a child of the
    * 'section' root element.
    * @param element the XML Node from which we are going to create the properties.
    * @return a Map of properties names and values mapped from the XML Node element.
    */
   private Map<String, String> getPropertiesFromNode(Node element) {
      Map<String, String> properties = new HashMap<String, String>(); 

      // retrieve the attributes of the element 
      NamedNodeMap attributes = element.getAttributes();
      
      // choose the strategy
      if (attributes.getLength() > 0) 
      {
         new TagNameAttributeMapper().map(element, properties);
      }
      else
      {
         new TagNameMapper().map(element, properties);
      }
      
      return properties;
   }
   
   /**
    * Retrieves the DOM document object from the inputStream.
    * @param inputStream the inputStream of the XML file.
    * @return a loaded Document object for DOM manipulation.
    * @throws Exception if the Document object couldn't be created.
    */
   private Document getDocument(InputStream inputStream) throws Exception 
   {
      Validate.notNull(inputStream, "No input stream specified");
      
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document document = db.parse(inputStream);
   
      document.getDocumentElement().normalize();
      
      return document;
   }
   
   
   
   /**
    * 
    * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
    */
   private interface PropertiesMapper 
   {
      void map(Node element, Map<String, String> properties);
   }

   /**
    * 
    * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
    */
   private class TagNameAttributeMapper implements PropertiesMapper
   {

      public void map(Node element, Map<String, String> properties)
      {
         // retrieve the attributes of the element 
         NamedNodeMap attributes = element.getAttributes();
         
         for (int k=0; k < attributes.getLength(); k++)
         {
            Node attribute = attributes.item(k);
            
            // build the property name
            String attributeName = attribute.getNodeName();
            String fullPropertyName = element.getLocalName() + Character.toUpperCase(attributeName.charAt(0)) 
                  + attributeName.substring(1);
           
            // add the property name and its value
            properties.put(fullPropertyName, attribute.getNodeValue());
         }
      }
   }
   
   /**
    * 
    * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
    */
   private class TagNameMapper implements PropertiesMapper
   {
      
      public void map(Node element, Map<String, String> properties)
      {
         String value = "";
         
         if (!element.hasChildNodes()) 
         {
            throw new ConfigurationException("Node " + element.getNodeName() + " has no value");
         }
         
         value = element.getChildNodes().item(0).getNodeValue();
         properties.put(element.getLocalName(), value);
      }
      
   }
}
