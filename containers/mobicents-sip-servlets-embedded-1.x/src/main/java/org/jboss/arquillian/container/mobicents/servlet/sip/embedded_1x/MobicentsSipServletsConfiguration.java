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
package org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1x;

import org.jboss.arquillian.container.tomcat.embedded_6.TomcatConfiguration;

/**
 * Arquillian Tomcat Container Configuration
 * 
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @author Dan Allen
 * @version $Revision: $
 */
public class MobicentsSipServletsConfiguration extends TomcatConfiguration
{
	private String sipConnectors = ":5080";
	private String sipApplicationRouterProviderClassName = "org.mobicents.servlet.sip.router.DefaultApplicationRouterProvider";
	

	/**
	 * @param sipApplicationRouterProviderClassName the sipApplicationRouterProviderClassName to set
	 */
	public void setSipApplicationRouterProviderClassName(
			String sipApplicationRouterProviderClassName) {
		this.sipApplicationRouterProviderClassName = sipApplicationRouterProviderClassName;
	}

	/**
	 * @return the sipApplicationRouterProviderClassName
	 */
	public String getSipApplicationRouterProviderClassName() {
		return sipApplicationRouterProviderClassName;
	}

	/**
	 * @param sipConnectors the sipConnectors to set
	 */
	public void setSipConnectors(String sipConnectors) {
		this.sipConnectors = sipConnectors;
	}

	/**
	 * @return the sipConnectors
	 */
	public String getSipConnectors() {
		return sipConnectors;
	}
	
	
}
