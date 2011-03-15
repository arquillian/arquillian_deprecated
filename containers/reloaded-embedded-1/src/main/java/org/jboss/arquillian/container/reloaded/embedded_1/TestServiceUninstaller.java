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
package org.jboss.arquillian.container.reloaded.embedded_1;

import org.jboss.arquillian.spi.core.Instance;
import org.jboss.arquillian.spi.core.annotation.Inject;
import org.jboss.arquillian.spi.core.annotation.Observes;
import org.jboss.arquillian.spi.event.container.BeforeUnDeploy;
import org.jboss.bootstrap.api.mc.server.MCServer;

/**
 * TestServiceUninstaller
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class TestServiceUninstaller 
{
   @Inject
   private Instance<MCServer> mcServer;

   public void uninstall(@Observes BeforeUnDeploy event)
   {
      MCServer server = mcServer.get();
      if(server != null)
      {
         server.getKernel().getController().uninstall(ReloadedTestEnricher.BIND_NAME_TEST);
      }
   }
}
