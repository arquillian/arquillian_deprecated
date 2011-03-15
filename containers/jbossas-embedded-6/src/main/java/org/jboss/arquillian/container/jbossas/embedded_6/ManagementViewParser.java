package org.jboss.arquillian.container.jbossas.embedded_6;
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


import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.protocol.metadata.Servlet;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.jboss.profileservice.spi.ProfileService;

/**
 * ManagementViewParser
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public final class ManagementViewParser
{
   public static ProtocolMetaData parse(String archiveName, ProfileService profile) throws Exception
   {
      ProtocolMetaData metaData = new ProtocolMetaData();

      ManagementView management = profile.getViewManager();
      management.load();

      // extract server info
      HTTPContext httpContext = extractHTTPContext(management);
      if (httpContext != null)
      {
         metaData.addContext(httpContext);
      }

      // extract deployment content
      scanDeployment(management, httpContext, management.getDeployment(archiveName));

      return metaData;
   }

   /**
    * @param management
    * @return
    */
   private static HTTPContext extractHTTPContext(ManagementView management) throws Exception
   {
      Set<String> contextRootDeployments = management.getMatchingDeploymentName("http\\-.*");
      if (contextRootDeployments.size() > 0)
      {
         String deploymentName = contextRootDeployments.iterator().next();
         String expression = ".*[A-Z]([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})-([0-9]+)";
         return new HTTPContext(deploymentName.replaceAll(expression, "$1"), Integer.parseInt(deploymentName
               .replaceAll(expression, "$2")));
      }
      return null;
   }

   private static void scanDeployment(ManagementView management, HTTPContext context, ManagedDeployment parent)
         throws NoSuchDeploymentException
   {
      Map<String, ManagedComponent> components = parent.getComponents();
      for (Map.Entry<String, ManagedComponent> entry : components.entrySet())
      {
         ManagedComponent value = entry.getValue();
         if (value.getType().getType().equals("WAR"))
         {
            scanWar(management, context, value);
         }
      }
      for (ManagedDeployment child : parent.getChildren())
      {
         scanDeployment(management, context, child);
      }
   }

   private static void scanWar(ManagementView management, HTTPContext context, ManagedComponent value)
         throws NoSuchDeploymentException
   {
      String contextRoot = value.getProperty("contextRoot").getField("value", String.class);
      Set<String> contextRootDeployments = management.getMatchingDeploymentName("//.*" + contextRoot);
      for (String contextRootDeployment : contextRootDeployments)
      {
         ManagedDeployment warDeployment = management.getDeployment(contextRootDeployment);
         for (Map.Entry<String, ManagedComponent> warComponentEntry : warDeployment.getComponents().entrySet())
         {
            ManagedComponent comp = warComponentEntry.getValue();
            if (comp.getType().getSubtype().equals("Servlet"))
            {
               String servletName = comp.getNameType().replaceFirst(".*,name=(.*)", "$1");
               context.add(new Servlet(servletName, contextRoot));
            }
         }
      }
   }
}
