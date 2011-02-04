/*
 * JBoss, Community-driven Open Source Middleware
 * Copyright 2010, JBoss by Red Hat, Inc., and individual contributors
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
package org.jboss.arquillian.container.openwebbeans.embedded_1;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class MockHttpSession implements HttpSession
{
   private final long creationTime;
   private final String id;
   private final Map<String, Object> attributes;
   private int maxInactiveInterval = 60000;

   public MockHttpSession()
   {
      creationTime = System.currentTimeMillis();
      id = UUID.randomUUID().toString();
      attributes = new HashMap<String, Object>();
   }

   public long getCreationTime()
   {
      return creationTime;
   }

   public String getId()
   {
      return id;
   }

   public long getLastAccessedTime()
   {
      return creationTime;
   }

   public ServletContext getServletContext()
   {
      throw new UnsupportedOperationException("Not supported.");
   }

   public void setMaxInactiveInterval(int i)
   {
      this.maxInactiveInterval = i;
   }

   public int getMaxInactiveInterval()
   {
      return maxInactiveInterval;
   }

   public HttpSessionContext getSessionContext()
   {
      throw new UnsupportedOperationException("Not supported.");
   }

   public Object getAttribute(String string)
   {
      return attributes.get(string);
   }

   public Object getValue(String string)
   {
      return getAttribute(string);
   }

   public Enumeration getAttributeNames()
   {
      final Iterator<String> nameIt = attributes.keySet().iterator();
      return new Enumeration() {

         public boolean hasMoreElements()
         {
            return nameIt.hasNext();
         }

         public Object nextElement()
         {
            return nameIt.next();
         }
      };
   }

   public String[] getValueNames()
   {
      return attributes.keySet().toArray(new String[0]);
   }

   public void setAttribute(String string, Object value)
   {
      attributes.put(string, value);
   }

   public void putValue(String string, Object value)
   {
      setAttribute(string, value);
   }

   public void removeAttribute(String string)
   {
      attributes.remove(string);
   }

   public void removeValue(String string)
   {
      removeAttribute(string);
   }

   public void invalidate()
   {
      attributes.clear();
   }

   public boolean isNew()
   {
      return true;
   }
}
