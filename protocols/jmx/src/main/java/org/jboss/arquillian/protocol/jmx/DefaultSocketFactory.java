/*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
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
package org.jboss.arquillian.protocol.jmx;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.server.RMIServerSocketFactory;
import javax.net.ServerSocketFactory;

/** An implementation of RMIServerSocketFactory that supports backlog and bind address settings
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 2787 $
 */
public class DefaultSocketFactory extends ServerSocketFactory implements RMIServerSocketFactory, Serializable
{
   static final long serialVersionUID = -7626239955727142958L;
   private transient InetAddress bindAddress;
   private int backlog = 200;

   /** Create a socket factory that binds on any address with a default
    * backlog of 200
    */
   public DefaultSocketFactory()
   {
      this(null, 200);
   }

   /** Create a socket factory with the given bind address
    * @param bindAddress 
    */
   public DefaultSocketFactory(InetAddress bindAddress)
   {
      this(bindAddress, 200);
   }

   /** Create a socket factory with the given backlog
    * @param backlog 
    */
   public DefaultSocketFactory(int backlog)
   {
      this(null, backlog);
   }

   /** Create a socket factory with the given bind address and backlog
    * @param bindAddress 
    * @param backlog 
    */
   public DefaultSocketFactory(InetAddress bindAddress, int backlog)
   {
      this.bindAddress = bindAddress;
      this.backlog = backlog;
   }

   public String getBindAddress()
   {
      String address = null;
      if (bindAddress != null)
         address = bindAddress.getHostAddress();
      return address;
   }

   public void setBindAddress(String host) throws UnknownHostException
   {
      bindAddress = InetAddress.getByName(host);
   }

   /**
    * Create a server socket on the specified port (port 0 indicates
    * an anonymous port).
    * @param  port the port number
    * @return the server socket on the specified port
    * @exception IOException if an I/O error occurs during server socket
    * creation
    * @since 1.2
    */
   public ServerSocket createServerSocket(int port) throws IOException
   {
      return createServerSocket(port, backlog, bindAddress);
   }

   /**
    * @param port - the port to listen to
    * @param backlog - how many connections are queued
    * @return A ServerSocket
    * @throws IOException
    */
   public ServerSocket createServerSocket(int port, int backlog) throws IOException
   {
      return createServerSocket(port, backlog, null);
   }

   /**
    * @param port - the port to listen to
    * @param backlog - how many connections are queued
    * @param inetAddress - the network interface address to use
    * @return the server socket
    * @throws IOException
    */
   public ServerSocket createServerSocket(int port, int backlog, InetAddress inetAddress) throws IOException
   {
      ServerSocket activeSocket = new ServerSocket(port, backlog, bindAddress);
      return activeSocket;
   }

   public boolean equals(Object obj)
   {
      boolean equals = obj instanceof DefaultSocketFactory;
      if (equals && bindAddress != null)
      {
         DefaultSocketFactory dsf = (DefaultSocketFactory)obj;
         InetAddress dsfa = dsf.bindAddress;
         if (dsfa != null)
            equals = bindAddress.equals(dsfa);
         else
            equals = false;
      }
      return equals;
   }

   public int hashCode()
   {
      int hashCode = getClass().getName().hashCode();
      if (bindAddress != null)
         hashCode += bindAddress.toString().hashCode();
      return hashCode;
   }

   public String toString()
   {
      StringBuffer tmp = new StringBuffer(super.toString());
      tmp.append('[');
      tmp.append("bindAddress=");
      tmp.append(bindAddress);
      tmp.append(']');
      return tmp.toString();
   }
}
