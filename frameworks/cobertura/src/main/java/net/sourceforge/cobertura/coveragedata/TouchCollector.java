/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2010 Piotr Tabor
 *
 * Note: This file is dual licensed under the GPL and the Apache
 * Source License (so that it can be used from both the main
 * Cobertura classes and the ant tasks).
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

package net.sourceforge.cobertura.coveragedata;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.cobertura.coveragedata.countermaps.AtomicCounterMap;
import net.sourceforge.cobertura.coveragedata.countermaps.CounterMap;

public class TouchCollector implements HasBeenInstrumented{
	
	private static final CounterMap<LineTouchData> touchedLines=new AtomicCounterMap<LineTouchData>();	
	private static final CounterMap<SwitchTouchData> switchTouchData=new AtomicCounterMap<SwitchTouchData>();	
	private static final CounterMap<JumpTouchData> jumpTouchData=new AtomicCounterMap<JumpTouchData>();
	
	private static AtomicInteger lastClassId=new AtomicInteger(1);
	private static final Map<String,Integer> class2classId=new ConcurrentHashMap<String, Integer>();
	private static final Map<Integer,String> classId2class=new ConcurrentHashMap<Integer,String>();

// We can handle dumping of data manually. No ShutDownHook required
//	static{
//		ProjectData.initialize();
//	}
	
	private static final int registerClassData(String name){		
		Integer res=class2classId.get(name);
		if (res==null){
			int new_id=lastClassId.incrementAndGet();
			class2classId.put(name, new_id);
			classId2class.put(new_id, name);
			return new_id;
		}
		return res;
	}
	
	/**
	 * This method is only called by code that has been instrumented.  It
	 * is not called by any of the Cobertura code or ant tasks.
	 */
	public static final void touchSwitch(String classId,int lineNumber, int switchNumber, int branch) {
		switchTouchData.incrementValue(new SwitchTouchData(registerClassData(classId),lineNumber, switchNumber, branch));
	}
	
	/**
	 * This method is only called by code that has been instrumented.  It
	 * is not called by any of the Cobertura code or ant tasks.
	 */
	public static final void touch(String classId,int lineNumber) {
		touchedLines.incrementValue(new LineTouchData(registerClassData(classId), lineNumber));
	}
	
	/**
	 * This method is only called by code that has been instrumented.  It
	 * is not called by any of the Cobertura code or ant tasks.
	 */
	public static final void touchJump(String classId,int lineNumber, int branchNumber, boolean branch) {
		jumpTouchData.incrementValue(new JumpTouchData(registerClassData(classId),lineNumber, branchNumber, branch));
	}	

	private static class LineTouchData implements HasBeenInstrumented{
		int classId,lineNumber;
		public LineTouchData(int classId,int lineNumber) {
			this.classId=classId;
			this.lineNumber=lineNumber;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + classId;
			result = prime * result + lineNumber;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LineTouchData other = (LineTouchData) obj;
			if (classId != other.classId)
				return false;
			if (lineNumber != other.lineNumber)
				return false;
			return true;
		}		
	}
	
	private static class SwitchTouchData extends LineTouchData implements HasBeenInstrumented{
		int switchNumber, branch;
		
		public SwitchTouchData(int classId,int lineNumber, int switchNumber, int branch) {
			super(classId,lineNumber);
			this.switchNumber=switchNumber;
			this.branch=branch;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + branch;
			result = prime * result + switchNumber;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SwitchTouchData other = (SwitchTouchData) obj;
			if (branch != other.branch)
				return false;
			if (switchNumber != other.switchNumber)
				return false;
			return true;
		}	
	}
	
	private static class JumpTouchData extends LineTouchData implements HasBeenInstrumented{
		int branchNumber;
		boolean branch;
		public JumpTouchData(int classId,int lineNumber, int branchNumber, boolean branch) {
			super(classId, lineNumber);
			this.branchNumber=branchNumber;
			this.branch=branch;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + (branch ? 1231 : 1237);
			result = prime * result + branchNumber;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			JumpTouchData other = (JumpTouchData) obj;
			if (branch != other.branch)
				return false;
			if (branchNumber != other.branchNumber)
				return false;
			return true;
		}		
	}	
	
	
	public static synchronized void applyTouchesOnProjectData(ProjectData projectData){
		//System.out.println("Flushing results...");
		Map<LineTouchData,Integer> touches=touchedLines.getFinalStateAndCleanIt();
		for(Entry<LineTouchData, Integer> touch:touches.entrySet()){
			if(touch.getValue()>0){				
				getClassFor(touch.getKey(),projectData).touch(touch.getKey().lineNumber,touch.getValue());
			}
		}
		
		Map<SwitchTouchData,Integer> switchTouches=switchTouchData.getFinalStateAndCleanIt();
		for(Entry<SwitchTouchData, Integer> touch:switchTouches.entrySet()){
			if(touch.getValue()>0){
				getClassFor(touch.getKey(),projectData).touchSwitch(
						touch.getKey().lineNumber,
						touch.getKey().switchNumber,
						touch.getKey().branch,touch.getValue());
			}
		}
		
		Map<JumpTouchData,Integer> jumpTouches=jumpTouchData.getFinalStateAndCleanIt();
		for(Entry<JumpTouchData, Integer> touch:jumpTouches.entrySet()){
			if(touch.getValue()>0){
				getClassFor(touch.getKey(),projectData).touchJump(
						touch.getKey().lineNumber,
						touch.getKey().branchNumber,
						touch.getKey().branch,touch.getValue());
			}
		}
		//System.out.println("Flushing results done");
	}

	private static ClassData getClassFor(LineTouchData key,ProjectData projectData) {
//		System.out.println("\nLooking for:"+key.classId+"\n");
		return projectData.getOrCreateClassData(classId2class.get(key.classId));
	}

}
