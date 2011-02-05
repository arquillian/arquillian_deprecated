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
package org.jboss.arquillian.container.weld.ee.embedded_1_1;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Expected;
import org.jboss.arquillian.container.weld.ee.embedded_1_1.beans.LoopingProducer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.exceptions.DeploymentException;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WeldEmbeddedDeploymentExceptionTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class WeldEmbeddedDeploymentExceptionTestCase
{
   @Deployment @Expected(DeploymentException.class)
   public static JavaArchive createDeployment()
   {
      return ShrinkWrap.create(JavaArchive.class)
                  .addClass(LoopingProducer.class)
                  .addManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }
   
   @Test // this method will be called even with deployment exception, @Expected marks DeploymentExceptions as acceptable behavior.
   public void shouldThrowDeploymentException() {}
}
