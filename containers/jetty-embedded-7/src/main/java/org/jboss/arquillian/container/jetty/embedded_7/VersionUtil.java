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
package org.jboss.arquillian.container.jetty.embedded_7;


/**
 * VersionUtil
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
class VersionUtil
{
   private VersionUtil() {}
   
   public static class Version implements Comparable<Version>
   {
      private Integer major;
      private Integer minor;
      
      public Version(int major, int minor)
      {
         this.major = major;
         this.minor = minor;
      }
      
      /**
       * @return the major
       */
      public int getMajor()
      {
         return major;
      }
      
      /**
       * @return the minor
       */
      public int getMinor()
      {
         return minor;
      }
      
      /* (non-Javadoc)
       * @see java.lang.Comparable#compareTo(java.lang.Object)
       */
      @Override
      public int compareTo(Version o)
      {
         int majorCompare = major.compareTo(o.major);
         if(majorCompare == 0)
         {
            return minor.compareTo(o.minor);
         }
         return majorCompare;
      }
   }
   
   private static String expression = "([0-9]{1,5})\\.([0-9]{1,5}).*";
   
   public static Version extract(String version)
   {
      if(version == null || !version.matches(expression))
      {
         return new Version(0, 0);
      }
      
      return new Version(
            Integer.parseInt(version.replaceAll(expression, "$1")), 
            Integer.parseInt(version.replaceAll(expression, "$2")));
   }

   public static boolean isGraterThenOrEqual(String greater, String then)
   {
      return isGraterThenOrEqual(extract(greater), extract(then));
   }

   public static boolean isGraterThenOrEqual(Version greater, Version then)
   {
      return greater.compareTo(then) >= 0;
   }
   
   public static boolean isLessThenOrEqual(String less, String then)
   {
      return isLessThenOrEqual(extract(less), extract(then));
   }

   public static boolean isLessThenOrEqual(Version less, Version then)
   {
      return less.compareTo(then) <= 0;
   }

}
