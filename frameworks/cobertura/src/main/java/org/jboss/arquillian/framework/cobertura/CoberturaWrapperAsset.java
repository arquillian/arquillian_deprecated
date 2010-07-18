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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.instrument.ClassInstrumenter;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.impl.base.Validate;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * CoberturaAsset
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CoberturaWrapperAsset implements Asset
{
   public static final List<ProjectData> PROJECT_DATA = new ArrayList<ProjectData>();
   
   private Asset asset;
   
   public CoberturaWrapperAsset(Asset asset)
   {
      Validate.notNull(asset, "Asset must be specified");
      this.asset = asset;
   }
   
   public InputStream openStream()
   {
      ProjectData projectData = new ProjectData();
      PROJECT_DATA.add(projectData);
      try
      {
         InputStream inputStream = asset.openStream();
         ClassReader cr = new ClassReader(inputStream);
         ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
         cr.accept(
               new ClassInstrumenter(projectData, cw, new ArrayList<Pattern>(), new ArrayList<Pattern>()), 
               0);
         
         return new  ByteArrayInputStream(cw.toByteArray());
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not instrument Asset " + asset, e);
      }
   }
}
