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
package org.jboss.arquillian.framework.jsfunit;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jsfunit.framework.Environment;
import org.jboss.jsfunit.jsfsession.JSFServerSession;
import org.jboss.jsfunit.jsfsession.JSFSession;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JSFUnitTestCase
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class JSFUnitTestCase
{
   @Deployment
   public static WebArchive createDeployment()
   {
      return ShrinkWrap.create(WebArchive.class, "test.war")
                  .addClasses(
                        RequestScopeBean.class, 
                        ScopeAwareBean.class)
                  .setWebXML("jsf/jsf-web.xml")
                  .addResource("jsf/index.xhtml", "index.xhtml")
                  .addWebResource(new ByteArrayAsset("<beans/>".getBytes()), ArchivePaths.create("beans.xml"));
   }

   @Test
   public void shouldExecutePage() throws Exception
   {
      JSFSession jsfSession = new JSFSession("/index.jsf");

      Assert.assertTrue(Environment.is12Compatible());
      Assert.assertTrue(Environment.is20Compatible());
      Assert.assertEquals(2, Environment.getJSFMajorVersion());
      Assert.assertEquals(0, Environment.getJSFMinorVersion());

      JSFServerSession server = jsfSession.getJSFServerSession();

      Assert.assertEquals("request", server.getManagedBeanValue("#{requestBean.scope}"));
   }

   @Test
   public void shouldExecutePage2() throws Exception
   {
      JSFSession jsfSession = new JSFSession("/index.jsf");

      Assert.assertTrue(Environment.is12Compatible());
      Assert.assertTrue(Environment.is20Compatible());
      Assert.assertEquals(2, Environment.getJSFMajorVersion());
      Assert.assertEquals(0, Environment.getJSFMinorVersion());

      JSFServerSession server = jsfSession.getJSFServerSession();

      Assert.assertEquals("request", server.getManagedBeanValue("#{requestBean.scope}"));
   }
}
