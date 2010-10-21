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
package org.jboss.arquillian.spi;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the global Arquillian configuration and the containers, extensions and protocols configuration . 
 * It is built by implementations of the {@link org.jboss.arquillian.impl.ConfigurationBuilder} interface.
 * 
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @version $Revision: $
 */
public class Configuration
{
   /**
    * Holds the configuration of containers
    */
   private Map<ContainerConfigKey, Map<String, String>> containersConfig = new HashMap<ContainerConfigKey, Map<String, String>>();

   /**
    * Holds the configuration of extensions
    */
   private Map<String, Map<String, String>> extensionsConfig = new HashMap<String, Map<String, String>>();
   
   /**
    * The qualifier of the active container, null if no active container was specified
    */
   private String activeContainerQualifier = null;

   private String deploymentExportPath = null;
   
   private int maxDeploymentsBeforeRestart = -1;
   
   /**
    * Stores a container configuration in the containersConfig map. If the package and qualifier 
    * already exists, it just replaces it.
    * 
    * @param packageName the name of the package that we will be used to match the container
    * configuration.
    * @param qualifier the qualifier of the container, null if the container has no qualifer.
    * @param properties a Map of properties that will be used to populate the container 
    * configuration.
    */
   public void addContainerConfig(String packageName, String qualifier, Map<String, String> properties)
   {
	   containersConfig.put(new ContainerConfigKey(packageName, qualifier), properties);	   
   }

   /**
    * Stores an extension configuration in the extensionsConfig map. If the package already
    * exists, it just replaces it. 
    * 
    * @param packageName the name of the package that will be used to match the container 
    * configuration.
    * @param properties a Map of properties that will be used to populate the container
    * configuration.
    */
   public void addExtensionConfig(String packageName, Map<String, String> properties)
   {
      extensionsConfig.put(packageName, properties);
   }
   
   /**
    * Populates a {@link ContainerConfiguration} object if there is a matching configuration for the 
    * container/qualifier. If not, it will leave the object as it was received.
    * 
    * @param container the {@link ContainerConfiguration} object to be populated.
    * @param qualifier used to match the container configuration.
    * @throws ConfigurationException wraps any exception thrown.
    */
   public void populateContainerConfig(ContainerConfiguration container, String qualifier) throws ConfigurationException
   {
      try 
      {
         // retrieve container
         String pkg = container.getClass().getPackage().getName();
         Map<String, String> properties = containersConfig.get(new ContainerConfigKey(pkg, qualifier));
         
         // if no configuration found, just return
         if (properties == null) {
             return;
         }
         
         // map the properties to the container
         populateObject(container, properties);
      }
      catch (Exception e) 
      {
         throw new ConfigurationException(e);
      }
   }

   /**
    * Populates a {@link ExtensionConfiguration} object if there is a matching configuration for the
    * extension. If not, it will leave the object as it was received.
    * 
    * @param extension the {@link ExtensionConfiguration} object to be populated.
    * @throws ConfigurationException wraps any exception thrown.
    */
   public void populateExtensionConfig(ExtensionConfiguration extension) throws ConfigurationException
   {
      try 
      {
         // retrieve the extension properties
         String pkg = extension.getClass().getPackage().getName();
         Map<String, String> properties = extensionsConfig.get(pkg);
         
         // if no configuration found, just return
         if (properties == null) {
             return;
         }
         
         // map the properties to the container
         populateObject(extension, properties);
      }
      catch (Exception e) 
      {
         throw new ConfigurationException(e);
      }
   }
   
   /**
    * Helper method. Populates the object with the properties received in the map.
    * 
    * @param object the object to be populated.
    * @param properties a Map of properties that will be used to populate the object
    * @throws Exception if something fails.
    */
   public static void populateObject(Object object, Map<String, String> properties) throws Exception 
   {
      Map<String, Method> setters = new HashMap<String, Method>();
      for (Method candidate : object.getClass().getMethods())
      {
         String methodName = candidate.getName();
         if (methodName.matches("^set[A-Z].*") &&
               candidate.getReturnType().equals(Void.TYPE) &&
               candidate.getParameterTypes().length == 1)
         {
            candidate.setAccessible(true);
            setters.put(methodName.substring(3, 4).toLowerCase() + methodName.substring(4), candidate);
         }
      }

      // set the properties found in the container XML fragment to the Configuration Object
      for (Map.Entry<String, String> property : properties.entrySet()) 
      {
         if (setters.containsKey(property.getKey()))
         {
            Method method = setters.get(property.getKey());
            Object value = convert(method.getParameterTypes()[0], property.getValue());
            method.invoke(object, value);
         } 
         else 
         {
            throw new IllegalArgumentException("property '" + property.getKey() + "' not found on class: " + object.getClass().getName()); 
         }
      }
   }
   
   /**
    * Converts a String value to the specified class.
    * 
    * @param clazz
    * @param value
    * @return
    */
   private static Object convert(Class<?> clazz, String value) 
   {
      /* TODO create a new Converter class and move this method there for reuse */
      
      if (Integer.class.equals(clazz) || int.class.equals(clazz)) 
      {
         return Integer.valueOf(value);
      } 
      else if (Double.class.equals(clazz) || double.class.equals(clazz)) 
      {
         return Double.valueOf(value);
      } 
      else if (Long.class.equals(clazz) || long.class.equals(clazz))
      {
         return Long.valueOf(value);
      }
      else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz))
      {
         return Boolean.valueOf(value);
      }
      
      return value;
   }

   /**
    * Get the active container qualifier of null if no active container has been specified.
    * 
    * @return the qualifier of the active container
    */
   public String getActiveContainerQualifier()
   {
      return activeContainerQualifier;
   }

   /**
    * Sets the active container qualifier.
    * 
    * @param activeContainerQualifier the qualifier of the active container.
    */
   public void setActiveContainerQualifier(String activeContainerQualifier)
   {
      this.activeContainerQualifier = activeContainerQualifier;
   }

   /**
    * Sets the Path used to export deployments.
    * 
    * @param deploymentExportPath String representation of path to use to export archives
    */
   public void setDeploymentExportPath(String deploymentExportPath)
   {
      this.deploymentExportPath = deploymentExportPath;
   }

   /**
    * Get the set export path for deployments.
    * @return Set path or null if not set
    */
   public String getDeploymentExportPath()
   {
      return deploymentExportPath;
   }

   /**
    * The max number of deployments to a container before restart is forced.
    * 
    * @return -1 if not set
    */
   public int getMaxDeploymentsBeforeRestart()
   {
      return maxDeploymentsBeforeRestart;
   }

   /**
    * Set how many deployments are allowed before a container restart is forced.
    * 
    * @param maxDeploymentsBeforeRestart number of deployments
    */
   public void setMaxDeploymentsBeforeRestart(int maxDeploymentsBeforeRestart)
   {
      this.maxDeploymentsBeforeRestart = maxDeploymentsBeforeRestart;
   }
   
   /**
    * This class is used as the key of the map of container configuration
    * 
    * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
    */
   class ContainerConfigKey {
	   
	   private String packageName;
	   
	   private String qualifier;
	   
	   public ContainerConfigKey(String packageName) {
		   this(packageName, null);
	   }
	   
	   public ContainerConfigKey(String packageName, String qualifier) {
		   this.packageName = packageName;
		   this.qualifier = qualifier;
	   }

		@Override
		public int hashCode() 
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((packageName == null) ? 0 : packageName.hashCode());
			result = prime * result
					+ ((qualifier == null) ? 0 : qualifier.hashCode());
			return result;
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ContainerConfigKey other = (ContainerConfigKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (packageName == null) 
			{
				if (other.packageName != null)
					return false;
			} 
			else if (!packageName.equals(other.packageName))
				return false;
			if (qualifier == null) 
			{
				if (other.qualifier != null)
					return false;
			} 
			else if (!qualifier.equals(other.qualifier))
				return false;
			return true;
		}
	
		private Configuration getOuterType() 
		{
			return Configuration.this;
		}
	   
   }
}
