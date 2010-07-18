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

import java.io.File;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.html.HTMLReport;
import net.sourceforge.cobertura.util.FileFinder;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.event.Event;
import org.jboss.arquillian.spi.event.suite.EventHandler;

/**
 * CoverageReportGenerator
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CoverageReportGenerator implements EventHandler<Event>
{
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.arquillian.spi.Context, java.lang.Object)
    */
   public void callback(Context context, Event event) throws Exception
   {
      // TODO: Need communication between Client and Server, https://jira.jboss.org/browse/ARQ-216
      ProjectData projectData = mergeCoverageFiles(new File(
            "/home/aslak/dev/source/testing/arquillian/frameworks/cobertura/target/report/"));
      
      synchronized (CoberturaWrapperAsset.PROJECT_DATA)
      {
         for(ProjectData assetData : CoberturaWrapperAsset.PROJECT_DATA)
         {
            projectData.merge(assetData);
         }
         CoberturaWrapperAsset.PROJECT_DATA.clear();
      }
      TouchCollector.applyTouchesOnProjectData(projectData);
      
      FileFinder finder = new FileFinder();
      // TODO: Need to be able to load configuration data, https://jira.jboss.org/browse/ARQ-215
      finder.addSourceDirectory("src/main/java");
      finder.addSourceDirectory("src/test/java");
      
      ComplexityCalculator complexity = new ComplexityCalculator(finder);
      
      // TODO: Need to be able to load configuration data, https://jira.jboss.org/browse/ARQ-215
      new HTMLReport(projectData, new File("target/coverage-report-html"), finder, complexity, "UTF-8");
   }

   private ProjectData mergeCoverageFiles(File outputDirectory)
   {
      ProjectData data = new ProjectData();
      File[] coverageFiles = outputDirectory.listFiles();
      for(File coverageFile : coverageFiles)
      {
         coverageFile.deleteOnExit();
         ProjectData snippet = CoverageDataFileHandler.loadCoverageData(coverageFile);
         data.merge(snippet);
      }
      return data;
   }
}