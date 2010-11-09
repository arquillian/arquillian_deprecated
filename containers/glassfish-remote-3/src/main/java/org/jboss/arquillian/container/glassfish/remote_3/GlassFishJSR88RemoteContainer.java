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
package org.jboss.arquillian.container.glassfish.remote_3;

import org.jboss.arquillian.container.jsr88.remote_1_2.JSR88Configuration;
import org.jboss.arquillian.container.jsr88.remote_1_2.JSR88RemoteContainer;
import org.jboss.arquillian.spi.ContainerMethodExecutor;
import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * An extension of the {@link JSR88RemoteContainer} for GlassFish
 *
 * <p>This class simply provides the {@link GlassFishJSR88Configuration} as
 * an override.</p>
 *
 * @author Dan Allen
 * @author Iskandar Salim
 */
public class GlassFishJSR88RemoteContainer extends JSR88RemoteContainer
{
   @Override
   public Class<? extends JSR88Configuration> getConfigurationClass()
   {
      return GlassFishJSR88Configuration.class;
   }

    @Override
    public ContainerMethodExecutor deploy(Context context, Archive<?> archive) throws DeploymentException {
       if (WebArchive.class.isInstance(archive))
       {
          //ArchivePath webXmlPath = ArchivePaths.create("/WEB-INF/web.xml");
          //if (!archive.contains(webXmlPath))
          //{
          //   // sets the module name to "test"
          //   ((WebArchive) archive).setWebXML("org/jboss/arquillian/container/glassfish/remote_3/web.xml");
          //}
          ArchivePath sunWebXmlPath = ArchivePaths.create("/WEB-INF/sun-web.xml");
          if (!archive.contains(sunWebXmlPath))
          {
             // sets the module name to "test"
             WebArchive.class.cast(archive).addWebResource("org/jboss/arquillian/container/glassfish/remote_3/sun-web.xml", "sun-web.xml");
          }
       }
       return super.deploy(context, archive);
    }

}
