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
package org.jboss.arquillian.container.jbossas.managed_5_1.utils;

import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerController;
import org.jboss.jbossas.servermanager.ServerManager;
import org.jboss.jbossas.servermanager.Argument;
import org.jboss.jbossas.servermanager.Property;

import java.io.File;

/**
 * @author <a href="mailto:aamonten@gmail.com">Alejandro Montenegro</a>
 */
public class AsLifecycleDelegate
{

   /*
    * Environment Variables
    */

   private static final String ENV_VAR_JAVA_HOME = "JAVA_HOME";

   private static final String ENV_VAR_JBOSS_HOME = "JBOSS_HOME";

   private ServerManager serverManager;

   /**
    * Constructor
    */
   public AsLifecycleDelegate()
   {
      // Create and set a new ServerManager
      ServerManager sm = new ServerManager();
      applyServerManagerDefaults(sm);
      this.setServerManager(sm);
   }

   /**
    * Lifecycle Start
    *
    * Starts JBossASs
    *
    * @throws Throwable
    */
   public void startJbossAs(String serverName) throws Throwable
   {
      Server server = null;

      // Get ServerManager
      ServerManager manager = this.getServerManager();

      try
      {
         server = manager.getServer(serverName);
      }
      catch (IllegalArgumentException e)
      {
         // Create the Server
         server = new Server();
         server.setName(serverName);

         // Add a Server to the Manager with defaults
         applyServerDefaults(server, manager);
      }

      // Start the Server
      ServerController.startServer(server, manager);
   }

   /**
    * Lifecycle Stop
    *
    * Stops JBossAS
    *
    * @throws Throwable
    */
   public void stopJbossAs(String serverName) throws Throwable
   {
      // Obtain the server
      ServerManager manager = this.getServerManager();
      Server server = manager.getServer(serverName);

      // If started/running
      if (ServerController.isServerStarted(server))
      {
         // Stop
         ServerController.stopServer(server, manager);
      }
   }

   /**
    * Apply defaults to ServerManager
    *
    * @param manager the server manager to apply defaults to
    * @return the server manager with applied defaults
    */
   public static ServerManager applyServerManagerDefaults(final ServerManager manager)
   {
      // Set JVM / JBOSS_HOME
      manager.setJavaHome(getJavaHome());
      manager.setJbossHome(getJbossHome());

      // Set UDP group to use
      // manager.setUdpGroup("241.34.53.227");

      return manager;
   }

   /**
    * Apply defaults to Server
    *
    * @param server the server to apply defaults to
    * @return the Server with applied defaults
    */
   public static Server applyServerDefaults(final Server server, final ServerManager manager)
   {
      // add Server to manager
      manager.addServer(server);

      server.setUsername("admin");
      server.setPassword("admin");
      server.setPartition(Long.toHexString(System.currentTimeMillis()));

      // Set server's JVM arguments
      Argument arg = new Argument();
      arg.setValue("-Xmx512m");
      server.addJvmArg(arg);
      arg = new Argument();
      arg.setValue("-XX:MaxPermSize=128m");
      server.addJvmArg(arg);

      // Set server's system properties
      Property prop = new Property();
      prop.setKey("jbosstest.udp.ip_ttl");
      prop.setValue("0");
      server.addSysProperty(prop);
      prop = new Property();
      prop.setKey("java.endorsed.dirs");
      prop.setValue(new File(manager.getJBossHome(), "lib/endorsed").getAbsolutePath());
      server.addSysProperty(prop);

      return server;
   }

   //----------------------------------------------------------------------------------||
   // Internal Helper Methods ---------------------------------------------------------||
   //----------------------------------------------------------------------------------||

   public static String getJavaHome()
   {
      return System.getenv(ENV_VAR_JAVA_HOME);
   }

   public static String getJbossHome()
   {
      return System.getenv(ENV_VAR_JBOSS_HOME);
   }

   public ServerManager getServerManager()
   {
      return serverManager;
   }

   private void setServerManager(ServerManager manager)
   {
      this.serverManager = manager;
   }
}
