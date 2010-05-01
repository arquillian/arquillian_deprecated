/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.arquillian.openejb.ejb;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;

/**
 * Implementation class of an EJB which returns request parameters
 * 
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Local(EchoLocalBusiness.class)
@RolesAllowed(
{})
@DeclareRoles(
{EchoBean.ROLE_ADMIN, "another"})
public class EchoBean implements EchoLocalBusiness
{
   //-------------------------------------------------------------------------------------||
   // Constants --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Admin role
    */
   public static final String ROLE_ADMIN = "Administrator";

   @Resource
   private SessionContext context;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.openejb.ejb.EchoLocalBusiness#securedEcho(java.lang.String)
    */
   @RolesAllowed(
   {ROLE_ADMIN})
   @Override
   public String securedEcho(final String value)
   {
      System.out.println(context.getCallerPrincipal().getName());
      return this.echo(value);
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.arquillian.openejb.ejb.EchoLocalBusiness#echo(java.lang.String)
    */
   @Override
   @PermitAll
   public String echo(final String value)
   {
      return value;
   }
}
