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
package org.jboss.arquillian.container.jbossas.remote_5_0;

import org.jboss.arquillian.spi.ConfigurationException;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;

/**
 * A {@link org.jboss.arquillian.spi.client.container.ContainerConfiguration} implementation for
 * the JBoss AS container.
 *
 * @author <a href="mailto:german.escobarc@gmail.com">German Escobar</a>
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JBossASConfiguration implements ContainerConfiguration
{
   /**
    * ProfileService profileKey. Used to load the correct profile into the DeploymentManager.  
    */
   private String profileName = "default";
      
   private String contextFactory = "org.jnp.interfaces.NamingContextFactory";
   
   private String urlPkgPrefix = "org.jboss.naming:org.jnp.interfaces";
   
   private String providerUrl = "jnp://localhost:1099";

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.ContainerConfiguration#validate()
    */
   @Override
   public void validate() throws ConfigurationException
   {
   }
   
   public String getProfileName()
   {
      return profileName;
   }
   
   public void setProfileName(String profileName)
   {
      this.profileName = profileName;
   }

   /**
    * @param contextFactory the contextFactory to set
    */
   public void setContextFactory(String contextFactory)
   {
      this.contextFactory = contextFactory;
   }
   
   /**
    * @return the contextFactory
    */
   public String getContextFactory()
   {
      return contextFactory;
   }
   
   /**
    * @param provierUrl the provierUrl to set
    */
   public void setProviderUrl(String provierUrl)
   {
      this.providerUrl = provierUrl;
   }
   
   /**
    * @return the provierUrl
    */
   public String getProviderUrl()
   {
      return providerUrl;
   }
   
   /**
    * @param urlPkgPrefix the urlPkgPrefix to set
    */
   public void setUrlPkgPrefix(String urlPkgPrefix)
   {
      this.urlPkgPrefix = urlPkgPrefix;
   }
   
   /**
    * @return the urlPkgPrefix
    */
   public String getUrlPkgPrefix()
   {
      return urlPkgPrefix;
   }
}
