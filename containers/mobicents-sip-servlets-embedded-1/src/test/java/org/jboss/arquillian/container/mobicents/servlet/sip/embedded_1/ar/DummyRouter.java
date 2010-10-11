/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.ar;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipTargetedRequestInfo;

/**
 * We provide a Dummy Router that does nothing but returning the only app deployed
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class DummyRouter implements SipApplicationRouter {

	String applicationName = null;
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ar.SipApplicationRouter#applicationDeployed(java.util.List)
	 */
	public void applicationDeployed(List<String> newlyDeployedApplicationNames) {
		if(newlyDeployedApplicationNames != null && !newlyDeployedApplicationNames.isEmpty()) {
			applicationName = newlyDeployedApplicationNames.get(0);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ar.SipApplicationRouter#applicationUndeployed(java.util.List)
	 */
	public void applicationUndeployed(List<String> undeployedApplicationNames) {
		applicationName = null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ar.SipApplicationRouter#destroy()
	 */
	public void destroy() {
		applicationName = null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ar.SipApplicationRouter#getNextApplication(javax.servlet.sip.SipServletRequest, javax.servlet.sip.ar.SipApplicationRoutingRegion, javax.servlet.sip.ar.SipApplicationRoutingDirective, javax.servlet.sip.ar.SipTargetedRequestInfo, java.io.Serializable)
	 */
	public SipApplicationRouterInfo getNextApplication(
			SipServletRequest initialRequest,
			SipApplicationRoutingRegion region,
			SipApplicationRoutingDirective directive,
			SipTargetedRequestInfo targetedRequestInfo, Serializable stateInfo) {
		return new SipApplicationRouterInfo(applicationName,null,null,null,null,null);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ar.SipApplicationRouter#init()
	 */
	public void init() {
		
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ar.SipApplicationRouter#init(java.util.Properties)
	 */
	public void init(Properties properties) {
	}

}
