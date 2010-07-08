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

import javax.enterprise.deploy.shared.ModuleType;
import static org.junit.Assert.*;

import org.jboss.arquillian.container.jsr88.remote_1_2.JSR88ModuleTypeMapper;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

/**
 * A test that verifies the {@link JSR88ModuleTypeMapper} returns
 * the correct JSR 88 {@link ModuleType} of a ShrinkWrap archive.
 *
 * @author Dan Allen
 */
public class JSR88ModuleTypeMapperTestCase
{
   @Test
   public void testWARModuleType()
   {
      Archive<?> archive = ShrinkWrap.create(WebArchive.class, "test.war");
      assertSame(ModuleType.WAR, getMapper().getModuleType(archive));
   }

   @Test
   public void testEARModuleType()
   {
      Archive<?> archive = ShrinkWrap.create(EnterpriseArchive.class, "test.ear");
      assertSame(ModuleType.EAR, getMapper().getModuleType(archive));
   }

   @Test
   public void testEJBModuleType()
   {
      Archive<?> archive = ShrinkWrap.create(JavaArchive.class ,"test.jar");
      assertSame(ModuleType.EJB, getMapper().getModuleType(archive));
   }

   @Test
   public void testRARModuleType()
   {
      Archive<?> archive = ShrinkWrap.create(ResourceAdapterArchive.class, "test.rar");
      assertSame(ModuleType.RAR, getMapper().getModuleType(archive));
   }

   @Test
   public void testSequentialUsage()
   {
      JSR88ModuleTypeMapper mapper = getMapper();
      Archive<?> rar = ShrinkWrap.create(ResourceAdapterArchive.class ,"test.rar");
      assertSame(ModuleType.RAR, mapper.getModuleType(rar));
      Archive<?> war = ShrinkWrap.create(WebArchive.class ,"test.war");
      assertSame(ModuleType.WAR, mapper.getModuleType(war));
   }

   private JSR88ModuleTypeMapper getMapper()
   {
      return new JSR88ModuleTypeMapper();
   }
}
