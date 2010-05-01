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
package org.jboss.arquillian.prototyping.context.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a contextual property with key / value
 * pair used to bolster type-specific resolution with some additional
 * metadata.
 *  
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(
{ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface Property {

   //-------------------------------------------------------------------------------------||
   // Fields -----------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The key of this property
    * @return
    */
   String key() default "";

   /**
    * The value of this property
    * @return
    */
   String value() default "";

}
