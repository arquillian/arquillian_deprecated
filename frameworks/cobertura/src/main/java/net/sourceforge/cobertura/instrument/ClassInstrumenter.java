/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2005 Mark Doliner 
 * Copyright (C) 2006 Jiri Mares 
 * 
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.instrument;

import java.util.Collection;
import java.util.logging.Logger;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ClassInstrumenter extends ClassAdapter
{

   private static final Logger logger = Logger.getLogger(ClassInstrumenter.class.getName());

   private final static String hasBeenInstrumented = "net/sourceforge/cobertura/coveragedata/HasBeenInstrumented";

   private final static String HAS_BEEN_INSTRUMENTED_FIELD_NAME = "___COBERTURA_INSTRUMENTED"; 
   
   private Collection ignoreRegexs;

   private Collection ignoreBranchesRegexs;

   private ProjectData projectData;

   private ClassData classData;

   private String myName;

   private boolean instrument = false;

   public String getClassName()
   {
      return this.myName;
   }

   public boolean isInstrumented()
   {
      return instrument;
   }

   public ClassInstrumenter(ProjectData projectData, final ClassVisitor cv, final Collection ignoreRegexs,
         final Collection ignoreBranchesRegexes)
   {
      super(cv);
      this.projectData = projectData;
      this.ignoreRegexs = ignoreRegexs;
      this.ignoreBranchesRegexs = ignoreBranchesRegexes;
   }

   private boolean arrayContains(Object[] array, Object key)
   {
      for (int i = 0; i < array.length; i++)
      {
         if (array[i].equals(key))
            return true;
      }

      return false;
   }

   /**
    * @param name In the format
    *             "net/sourceforge/cobertura/coverage/ClassInstrumenter"
    */
   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
   {
      this.myName = name.replace('/', '.');
      this.classData = this.projectData.getOrCreateClassData(this.myName);
      this.classData.setContainsInstrumentationInfo();

      // Do not attempt to instrument interfaces or classes that
      // have already been instrumented
      
      
      
      if (((access & Opcodes.ACC_INTERFACE) != 0) || arrayContains(interfaces, hasBeenInstrumented))
      {
         super.visit(version, access, name, signature, superName, interfaces);
      }
      else
      {
         instrument = true;

//         // Flag this class as having been instrumented
//         String[] newInterfaces = new String[interfaces.length + 1];
//         System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
//         newInterfaces[newInterfaces.length - 1] = hasBeenInstrumented;

         super.visit(version, access, name, signature, superName, interfaces);
      }
   }

   /**
    * @param source In the format "ClassInstrumenter.java"
    */
   public void visitSource(String source, String debug)
   {
      super.visitSource(source, debug);
      classData.setSourceFileName(source);
   }

   @Override
   public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) 
   {
      if(HAS_BEEN_INSTRUMENTED_FIELD_NAME.equals(name))
      {
         instrument = false;
      }
      return super.visitField(access, name, desc, signature, value);
   }
   
   public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
         final String[] exceptions)
   {
      MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

      if (!instrument)
         return mv;

      return mv == null ? null : new FirstPassMethodInstrumenter(classData, mv, this.myName, access, name, desc,
            signature, exceptions, ignoreRegexs, ignoreBranchesRegexs);
   }

   public void visitEnd()
   {
      if(instrument)
      {
         FieldVisitor visitor = super.visitField(
               Opcodes.ACC_PRIVATE & Opcodes.ACC_STATIC & Opcodes.ACC_FINAL, 
               HAS_BEEN_INSTRUMENTED_FIELD_NAME, 
               Type.BOOLEAN_TYPE.toString(), 
               null, 
               true);
         visitor.visitEnd();
      }
      
      if (instrument && classData.getNumberOfValidLines() == 0)
         logger.warning("No line number information found for class " + this.myName
               + ".  Perhaps you need to compile with debug=true?");
   }

}
