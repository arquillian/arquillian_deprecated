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
package org.jboss.arquillian.container.jsr88.remote_1_2;

import java.util.logging.Logger;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;

/**
 * Listens for JSR 88 deployment events to update the deployed state
 * of the module.
 *
 * <p>During distribution (deployment), this listener observes the completed
 * operation and subsequently starts the module, marking the module as
 * started when the start operation is complete.</p>
 *
 * <p>During undeployment, this listener observes the completed operation
 * and marks the module as not started.</p>
 *
 * @author Dan Allen
 * @author Iskandar Salim
 */
class JSR88DeploymentListener implements ProgressListener
{
   private static final Logger log = Logger.getLogger(JSR88RemoteContainer.class.getName());

   private JSR88RemoteContainer container;
   private TargetModuleID[] ids;
   private CommandType type;

   JSR88DeploymentListener(JSR88RemoteContainer container, TargetModuleID[] moduleIds, CommandType type)
   {
      this.container = container;
      this.ids = moduleIds;
      this.type = type;
   }

   public void handleProgressEvent(ProgressEvent event)
   {
      DeploymentStatus status = event.getDeploymentStatus();
      log.info(status.getMessage());
      if (status.isCompleted())
      {
         if (type.equals(CommandType.DISTRIBUTE))
         {
            ProgressObject startProgress = container.getDeploymentManager().start(ids);
            startProgress.addProgressListener(new ProgressListener()
            {
               public void handleProgressEvent(ProgressEvent startEvent)
               {
                  log.info(startEvent.getDeploymentStatus().getMessage());
                  if (startEvent.getDeploymentStatus().isCompleted())
                  {
                     container.moduleStarted(true);
                  }
               }
            });
         }
         else if (type.equals(CommandType.UNDEPLOY))
         {
            container.moduleStarted(false);
         }
      }
   }
}
