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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.jsfunit.seam.SeamUtil;

/**
 * JSFUnitCleanupTestTreadFilter
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class JSFUnitCleanupTestTreadFilter implements Filter
{

   public void init(FilterConfig arg0) throws ServletException
   {
   }

   public void destroy()
   {
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
   {
      chain.doFilter(request, response);
      cleanUp((HttpServletRequest)request);
   }

   private void cleanUp(HttpServletRequest httpServletRequest)
   {
      if (!isCallTestService(httpServletRequest)) return;
      
      if (SeamUtil.isSeamInitialized())
      {
         SeamUtil.invalidateSeamSession(httpServletRequest);
      }
      
      HttpSession session = httpServletRequest.getSession(false);
      if (session != null)
      {
         session.invalidate();
      }
   }

   // cleanUp should only be done if Cactus is running a CALL_TEST_SERVICE command
   private boolean isCallTestService(HttpServletRequest httpServletRequest)
   {
      return true;
   }
}
