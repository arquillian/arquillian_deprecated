/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.jsfunit.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKit;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

/**
 * This class is a wrapper for the "real" FacesContext.
 *
 * @author Stan Silvert
 * @since 1.0
 */
public class JSFUnitFacesContext extends FacesContext implements HttpSessionBindingListener, Serializable
{
   public static final String SESSION_KEY = JSFUnitFacesContext.class.getName() + ".sessionkey";
   
   // This is the wrapped FacesContext instance.
   private FacesContext delegate;
   
   // initialized after the JSF lifecycle is over
   private ExternalContext extContext = null;

   // Must save FacesMessages for use when request is over: JSFUNIT-82
   private List<FacesMessage> allMessages = new ArrayList<FacesMessage>();
   private Map<String, List<FacesMessage>> messagesByClientId = new HashMap<String, List<FacesMessage>>();
   
   public JSFUnitFacesContext(FacesContext delegate)
   {
      if (delegate == null) throw new NullPointerException("delegate can not be null.");
      
      this.delegate = delegate;
      setCurrentInstance(this);
   }

   @Override
   public boolean isProjectStage(ProjectStage projectStage)
   {
      return delegate.isProjectStage(projectStage);
   }
   
   @Override
   public Iterator getMessages(String clientId)
   {
      if (!isJSFRequestDone()) return delegate.getMessages(clientId);
      
      List<FacesMessage> messages = this.messagesByClientId.get(clientId);
      if (messages == null) return new ArrayList().iterator();
      
      return messages.iterator();
   }
   
   @Override
   public void addMessage(String clientId, FacesMessage facesMessage)
   {
      delegate.addMessage(clientId, facesMessage);
      
      // save FacesMessages for when the request is done
      this.allMessages.add(facesMessage);
      List<FacesMessage> messageList = messagesByClientId.get(clientId);
      if (messageList == null) messageList = new ArrayList<FacesMessage>();
      messageList.add(facesMessage);
      messagesByClientId.put(clientId, messageList);
   }
   
   @Override
   public void setResponseWriter(ResponseWriter responseWriter)
   {
      delegate.setResponseWriter(responseWriter);
   }
   
   @Override
   public void setResponseStream(ResponseStream responseStream)
   {
      delegate.setResponseStream(responseStream);
   }
   
   @Override
   public void setViewRoot(UIViewRoot uIViewRoot)
   {
      delegate.setViewRoot(uIViewRoot);
   }
   
   @Override
   public void responseComplete()
   {
      delegate.responseComplete();
   }
   
   @Override
   public void renderResponse()
   {
      delegate.renderResponse();
   }
   
   @Override
   public Application getApplication()
   {
      return delegate.getApplication();
   }
   
   @Override
   public Iterator getClientIdsWithMessages()
   {
      return delegate.getClientIdsWithMessages();
   }
   
   @Override
   public ExternalContext getExternalContext()
   {
      if (!isJSFRequestDone())
      {
         return new JSFUnitDelegatingExternalContext(delegate.getExternalContext());
      }
      
      return this.extContext;
   }
   
   @Override
   public FacesMessage.Severity getMaximumSeverity()
   {
      return delegate.getMaximumSeverity();
   }
   
   @Override
   public Iterator getMessages()
   {
      if (!isJSFRequestDone()) return delegate.getMessages();
      
      return this.allMessages.iterator();
   }
   
   @Override
   public RenderKit getRenderKit()
   {
      return delegate.getRenderKit();
   }
   
   @Override
   public boolean getRenderResponse()
   {
      return delegate.getRenderResponse();
   }
   
   @Override
   public boolean getResponseComplete()
   {
      return delegate.getResponseComplete();
   }
   
   @Override
   public ResponseStream getResponseStream()
   {
      return delegate.getResponseStream();
   }
   
   @Override
   public ResponseWriter getResponseWriter()
   {
      return delegate.getResponseWriter();
   }
   
   @Override
   public UIViewRoot getViewRoot()
   {
      return delegate.getViewRoot();
   }
   
   /**
    * This is called when the JSF lifecycle is over.  After this is called,
    * most operations will still delegate to the wrapped FacesContext, but
    * the ExternalContext will be replaced with a JSFUnitExternalContext.
    *
    * This method does not call release() on the wrapped FacesContext.  So, all
    * of its state is retained for use by JSFUnit tests.
    */
   @Override
   public void release()
   {
      // Make the FacesContext available to JSFUnit, if and only if a new
      // page was rendered.
      if (!viewHasChildren())
      {
         cleanUp();
         return;
      }
      
      ExternalContext extCtx = delegate.getExternalContext();
      this.extContext = new JSFUnitExternalContext(extCtx);
      extCtx.getSessionMap().put(SESSION_KEY, this);
      
      // Clean local Thread variable to prevent leak on the HTTP Threads. 
      setCurrentInstance(null);
   }
   
   //-------- JSF 2.0 -----------------------------------------
   @Override
   public Map<Object, Object> getAttributes() 
   {
      return this.delegate.getAttributes();
   }

   @Override
   public PhaseId getCurrentPhaseId() 
   {
      return this.delegate.getCurrentPhaseId();
   }

   @Override
   public boolean isPostback() 
   {
      return this.delegate.isPostback();
   }

   @Override
   public void setCurrentPhaseId(PhaseId phaseId) 
   {
      this.delegate.setCurrentPhaseId(phaseId);
   }

   @Override
   public ExceptionHandler getExceptionHandler()
   {
      return delegate.getExceptionHandler();
   }

   @Override
   public List<FacesMessage> getMessageList()
   {
      return delegate.getMessageList();
   }

   @Override
   public List<FacesMessage> getMessageList(String clientId)
   {
      return delegate.getMessageList(clientId);
   }

   @Override
   public PartialViewContext getPartialViewContext()
   {
      return delegate.getPartialViewContext();
   }

   @Override
   public boolean isProcessingEvents()
   {
      return delegate.isProcessingEvents();
   }

   @Override
   public boolean isValidationFailed()
   {
      return delegate.isValidationFailed();
   }

   @Override
   public void setExceptionHandler(ExceptionHandler exceptionHandler)
   {
      delegate.setExceptionHandler(exceptionHandler);
   }

   @Override
   public void setProcessingEvents(boolean processingEvents)
   {
      delegate.setProcessingEvents(processingEvents);
   }

   @Override
   public void validationFailed()
   {
      delegate.validationFailed();
   }

      
   //-----End JSF 2.0 Methods-----------------------------------------------------
   private boolean viewHasChildren()
   {
      UIViewRoot viewRoot = getViewRoot();
      return (viewRoot != null) && (viewRoot.getChildCount() != 0);
   }
   
   /**
    * This allows the FacesContextBridge to associate the JSFUnitFacesContext
    * with the thread running the tests.
    */
   public void setInstanceToJSFUnitThread()
   {
      setCurrentInstance(this);
   }
   
   public boolean isJSFRequestDone()
   {
      return this.extContext != null;
   }
   
   @Override
   public ELContext getELContext()
   {
      ELContext elContext = delegate.getELContext();
      
      // if JSF lifecycle is over we are using the JSFUnitFacesContext
      // instead of the delegate.  So we need to replace it in ELContext
      if (isJSFRequestDone())
      {
         elContext.putContext(FacesContext.class, this);
      }
      
      return elContext;
   }
   
   /**
    * This static method will attempt to clean up any FacesContext that is
    * associated with the current thread.
    */
   public static void cleanUpOldFacesContext()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if (facesContext == null) return;
      
      if (facesContext instanceof JSFUnitFacesContext)
      {
         JSFUnitFacesContext ctx = (JSFUnitFacesContext)facesContext;
         ctx.cleanUp();
      } else {
         facesContext.release();
      }
   }
   
   private synchronized void cleanUp()
   {
      try
      {
         delegate.release();
      }
      catch (Exception e)
      {
         // ignore - best effort to clean up delegate
      }
      finally
      {
         setCurrentInstance(null);
      }
   }
   
   /**
    * Attempt to clean up the previous FacesContext.
    */
   //@Override
   public void valueUnbound(HttpSessionBindingEvent httpSessionBindingEvent)
   {
      cleanUp();
   }
   
   //@Override
   public void valueBound(HttpSessionBindingEvent httpSessionBindingEvent)
   {
      // do nothing
   }
   
}
