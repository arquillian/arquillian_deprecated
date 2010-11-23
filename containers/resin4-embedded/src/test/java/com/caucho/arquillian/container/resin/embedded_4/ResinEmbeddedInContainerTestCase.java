/*
 * Copyright (c) 1998-2010 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Dominik Dorn
 */
package com.caucho.arquillian.container.resin.embedded_4;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.logging.Logger;

/**
 * In-container test case for the Resin 4 Embedded container.
 * Based on the Jetty7 Test by Dan Allen
 *
 * @author Dominik Dorn
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class ResinEmbeddedInContainerTestCase
{
   private static final Logger log = Logger.getLogger(ResinEmbeddedInContainerTestCase.class.getName());
   
   /**
    * Deployment for the test
    */
   @Deployment
   public static WebArchive getTestArchive()
   {
      final WebArchive war = ShrinkWrap.create(WebArchive.class, "test.war")
         .addClass(TestBean.class)
         .addLibrary(MavenArtifactResolver.resolve("org.jboss.arquillian.protocol:arquillian-protocol-servlet-3:1.0.0-SNAPSHOT"))
         .addWebResource("resin-env.xml", "resin-web.xml") // ads and renames resin-env.xml to resin-web.xml
         .addWebResource(new ByteArrayAsset(new byte[0]), "beans.xml")
         .setWebXML("in-container-web.xml");
       log.info("created war file: ");
       log.info(war.toString(true));
      return war;
   }

   // defined in resin-env.xml, scoped to global
   @Resource(mappedName = "version")
   Integer version;

   // defined in web.xml, scoped to webapp (relative to java:comp/env)
   @Resource(name = "name")
   String name;

   // defined in resin-env.xml, scoped to webapp (relative to java:comp/env)
   @Resource(name = "type")
   String containerType;

   @Resource(name = "jdbc/test")
   DataSource ds;

   @Inject
   TestBean testBean;

   @Test
   public void shouldBeAbleToInjectMembersIntoTestClass() throws Exception
   {
      Assert.assertNotNull(version);
      Assert.assertEquals(new Integer(4), version);
      Assert.assertNotNull(name);
      Assert.assertEquals("Resin", name);
      Assert.assertNotNull(containerType);
      Assert.assertEquals("Embedded", containerType);
      Assert.assertNotNull(ds);
      Connection c = null;
      try
      {
         c = ds.getConnection();
         Assert.assertEquals("H2", c.getMetaData().getDatabaseProductName());
         c.close();
      }
      catch (Exception e)
      {
         Assert.fail(e.getMessage());
      }
      finally
      {
         if (c != null && !c.isClosed())
         {
            c.close();
         }
      }
      Assert.assertNotNull(testBean);
      Assert.assertEquals("Resin", testBean.getName());
   }
}
