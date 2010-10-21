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

import java.util.Collections;

import junit.framework.Assert;

import org.jboss.arquillian.spi.Configuration;
import org.jboss.arquillian.spi.ConfigurationException;
import org.jboss.arquillian.spi.ContainerConfiguration;
import org.jboss.arquillian.spi.ContainerProfile;
import org.jboss.arquillian.spi.ExtensionConfiguration;
import org.junit.Test;

/**
 * 
 * 
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 */
public class ConfigurationTestCase
{
   
   @Test
   public void testExistentContainerConfig() throws Exception 
   {
      MockConfig mockContainerConfig = new MockConfig();
      
      Configuration configuration = new Configuration();
      configuration.addContainerConfig(MockConfig.class.getPackage().getName(), null, 
            Collections.singletonMap("config1", "notdefault"));
      
      configuration.populateContainerConfig(mockContainerConfig, null);
      
      Assert.assertEquals("notdefault", mockContainerConfig.getConfig1());
   }
   
   @Test
   public void testExistentContainerConfigWithQualifier() throws Exception 
   {
      MockConfig mockContainerConfig1 = new MockConfig();
      MockConfig mockContainerConfig2 = new MockConfig();
      
      Configuration configuration = new Configuration();
      configuration.addContainerConfig(MockConfig.class.getPackage().getName(), null, 
            Collections.singletonMap("config1", "mock1"));
      configuration.addContainerConfig(MockConfig.class.getPackage().getName(), "qual1", 
            Collections.singletonMap("config1", "notdefault"));
      
      configuration.populateContainerConfig(mockContainerConfig1, null);
      configuration.populateContainerConfig(mockContainerConfig2, "qual1");
      
      Assert.assertEquals("mock1", mockContainerConfig1.getConfig1());
      Assert.assertEquals("notdefault", mockContainerConfig2.getConfig1());
   }
   
   @Test
   public void testNonExistentContainerConfig() throws Exception 
   {
      MockConfig mockContainerConfig = new MockConfig();
      
      Configuration configuration = new Configuration();
      configuration.populateContainerConfig(mockContainerConfig, null);
      
      Assert.assertEquals("default", mockContainerConfig.getConfig1());
   }
   
   @Test(expected=ConfigurationException.class)
   public void shouldFailInvalidContainerConfig() throws Exception 
   {
      MockConfig mockContainerConfig = new MockConfig();
      
      Configuration configuration = new Configuration();
      configuration.addContainerConfig(MockConfig.class.getPackage().getName(), null, 
            Collections.singletonMap("config", "notdefault"));
      
      configuration.populateContainerConfig(mockContainerConfig, null);
   }
   
   @Test
   public void testExistentExtensionConfig() throws Exception 
   {
      MockConfig mockExtensionConfig = new MockConfig();
      
      Configuration configuration = new Configuration();
      configuration.addExtensionConfig(MockConfig.class.getPackage().getName(), 
            Collections.singletonMap("config1", "notdefault"));
      
      configuration.populateExtensionConfig(mockExtensionConfig);
      
      Assert.assertEquals("notdefault", mockExtensionConfig.getConfig1());
   }
   
   @Test
   public void testNonExistentExtensionConfig() throws Exception 
   {
      MockConfig mockExtensionConfig = new MockConfig();
      
      Configuration configuration = new Configuration();
      configuration.populateExtensionConfig(mockExtensionConfig);
      
      Assert.assertEquals("default", mockExtensionConfig.getConfig1());
   }
   
   @Test(expected=ConfigurationException.class)
   public void shouldFailInvalidExtensionConfig() throws Exception 
   {
      MockConfig mockExtensionConfig = new MockConfig();
      
      Configuration configuration = new Configuration();
      configuration.addExtensionConfig(MockConfig.class.getPackage().getName(), 
            Collections.singletonMap("config", "notdefault"));
      
      configuration.populateExtensionConfig(mockExtensionConfig);
   }
   
   private class MockConfig implements ContainerConfiguration, ExtensionConfiguration
   {
      
      private String config1 = "default";

      public String getConfig1()
      {
         return config1;
      }

      @SuppressWarnings("unused")
      public void setConfig1(String config1)
      {
         this.config1 = config1;
      }

      public ContainerProfile getContainerProfile()
      {
         // TODO Auto-generated method stub
         return null;
      }
      
   }

   
}
