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
package org.jboss.arquillian.impl.configuration;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.impl.configuration.api.DefaultProtocolDef;
import org.jboss.shrinkwrap.descriptor.api.Node;

/**
 * DefaultProtocolDefImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DefaultProtocolDefImpl extends ArquillianDescriptorImpl implements DefaultProtocolDef
{
   private Node protocol;

   public DefaultProtocolDefImpl(Node model, Node protocol)
   {
      super(model);
      this.protocol = protocol;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations - ProtocolDescription -------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.DefaultProtocolDef#setType(java.lang.String)
    */
   @Override
   public DefaultProtocolDef setType(String type)
   {
      protocol.attribute("type", type);
      return this;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.DefaultProtocolDef#getType()
    */
   @Override
   public String getType()
   {
      return protocol.attribute("type");
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.impl.configuration.api.ProtocolDescription#property(java.lang.String, java.lang.String)
    */
   @Override
   public DefaultProtocolDef property(String name, String value)
   {
      protocol.getOrCreate("configuration").create("property").attribute("name", name).text(value);
      return this;
   }
   
   public Map<String, String> getProperties()
   {
      Node props = protocol.getSingle("configuration");
      Map<String, String> properties = new HashMap<String, String>();
      
      if(props != null)
      {
         for(Node prop: props.get("property"))
         {
            properties.put(prop.attribute("name"), prop.text());
         }
      }
      return properties;
   }
}
