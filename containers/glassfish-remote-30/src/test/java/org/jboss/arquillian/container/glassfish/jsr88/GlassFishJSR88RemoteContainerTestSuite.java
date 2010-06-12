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
package org.jboss.arquillian.container.glassfish.jsr88;

import java.io.File;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * A JUnit 4 test suite that manages the lifecycle of a locally-installed
 * GlassFish instance in order to test the JSR 88 deployment.
 *
 * <p>This class relies on the system property glassfish.install.dir
 * to point to a valid installation of GlassFish v3.</p>
 *
 * @author Dan Allen
 */
@RunWith(Suite.class)
@SuiteClasses({
   GlassFishJSR88RemoteContainerWARTestCase.class,
   GlassFishJSR88RemoteContainerEARTestCase.class})
public class GlassFishJSR88RemoteContainerTestSuite
{
   @BeforeClass
   public static void startContainer() throws Exception
   {
      Runtime.getRuntime().exec(new String[] {
            getAsadminCommand(),
            "start-domain",
            "tests"}).waitFor();
   }

   @AfterClass
   public static void stopContainer() throws Exception
   {
      Runtime.getRuntime().exec(new String[] {
            getAsadminCommand(),
            "stop-domain",
            "tests"}).waitFor();
   }

   public static String getAsadminCommand()
   {
      File asadminFile = new File(System.getProperty("glassfish.install.dir") +
            File.separator + "bin" + File.separator + "asadmin");
      if (!asadminFile.exists() || !asadminFile.isFile())
      {
         Assert.fail("Path to asadmin command is invalid: " + asadminFile.getAbsolutePath());
      }
      return asadminFile.getAbsolutePath();
   }
}
