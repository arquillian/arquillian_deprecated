/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.container.jetty.embedded_7;

import org.jboss.arquillian.container.jetty.embedded_7.VersionUtil.Version;
import org.junit.Assert;
import org.junit.Test;


/**
 * VersionUtilTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class VersionUtilTestCase
{

   @Test
   public void shouldBeAbleToExtract() throws Exception
   {
      Version version = VersionUtil.extract("1.2");
      Assert.assertEquals(1, version.getMajor());
      Assert.assertEquals(2, version.getMinor());
   }

   @Test
   public void shouldBeAbleToExtractWithMultipleDigits() throws Exception
   {
      Version version = VersionUtil.extract("10.300");
      Assert.assertEquals(10, version.getMajor());
      Assert.assertEquals(300, version.getMinor());
   }

   @Test
   public void shouldBeAbleToExtractWithBuild() throws Exception
   {
      Version version = VersionUtil.extract("1.2.50.A");
      Assert.assertEquals(1, version.getMajor());
      Assert.assertEquals(2, version.getMinor());
   }

   @Test
   public void shouldReturnZeroVersionOnNull() throws Exception
   {
      Version version = VersionUtil.extract(null);
      Assert.assertEquals(0, version.getMajor());
      Assert.assertEquals(0, version.getMinor());
   }

   @Test
   public void shouldReturnZeroVersionOnNullUnMatched() throws Exception
   {
      Version version = VersionUtil.extract("243223.A");
      Assert.assertEquals(0, version.getMajor());
      Assert.assertEquals(0, version.getMinor());
   }
   
   @Test
   public void shouldBeGreaterEqual() 
   {
      Version greater = VersionUtil.extract("7.1");
      Version then = VersionUtil.extract("7.1");
   
      Assert.assertTrue(VersionUtil.isGraterThenOrEqual(greater, then));
   }

   @Test
   public void shouldBeGreaterThen() 
   {
      Version greater = VersionUtil.extract("7.2");
      Version then = VersionUtil.extract("7.1");
   
      Assert.assertTrue(VersionUtil.isGraterThenOrEqual(greater, then));
   }

   @Test
   public void shouldBeLessEqual() 
   {
      Version less = VersionUtil.extract("7.1");
      Version then = VersionUtil.extract("7.1");
   
      Assert.assertTrue(VersionUtil.isLessThenOrEqual(less, then));
   }

   @Test
   public void shouldBeLessThen() 
   {
      Version less = VersionUtil.extract("7.1");
      Version then = VersionUtil.extract("7.2");
   
      Assert.assertTrue(VersionUtil.isLessThenOrEqual(less, then));
   }
}
