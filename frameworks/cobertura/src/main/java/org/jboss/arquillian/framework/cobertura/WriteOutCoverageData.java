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
package org.jboss.arquillian.framework.cobertura;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * DumpCoverageData
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class WriteOutCoverageData implements EventHandler<ClassEvent>
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, ClassEvent event) throws Exception
   {
      ProjectData projectData = new ProjectData();
      TouchCollector.applyTouchesOnProjectData(projectData);
      
      // TODO: https://jira.jboss.org/browse/ARQ-216
      //context.get(ClientCommunicator.class).send(CoberturaClient.class, projectData);
      
      write(projectData);
   }

   private void write(ProjectData projectData)
   {
      ObjectOutputStream stream = null;
      try
      {
         // TODO: create a communication service for messaging between Container and client, https://jira.jboss.org/browse/ARQ-216
         stream = new ObjectOutputStream(
               new FileOutputStream(
                     "/home/aslak/dev/source/testing/arquillian/frameworks/cobertura/target/report/" + 
                     UUID.randomUUID().toString()));
         stream.writeObject(projectData);
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not write out coverage data", e);
      } 
      finally
      {
         if(stream != null)
         {
            try
            {
               stream.close();
            }
            catch (Exception e) 
            {
               throw new RuntimeException("Could not close coverage stream", e);
            }
         }
      }
   }
}
