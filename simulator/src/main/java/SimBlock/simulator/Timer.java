/**
 * Copyright 2019 Distributed Systems Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package SimBlock.simulator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import SimBlock.task.Task;


public class Timer {
	
	private static PriorityQueue<ScheduledTask> taskQueue = new PriorityQueue<ScheduledTask>();
	private static Map<Task,ScheduledTask> taskMap = new HashMap<Task,ScheduledTask>();
	private static BigDecimal currentTime = BigDecimal.ZERO;
	
	private static class ScheduledTask implements Comparable<ScheduledTask> {
		private final Task task;
		private final BigDecimal scheduledTime;
		
		private ScheduledTask(Task task, BigDecimal scheduledTime){
			this.task = task;
			this.scheduledTime = scheduledTime;
		}
		
		private Task getTask(){ return this.task; }
		private BigDecimal getScheduledTime(){ return this.scheduledTime; }
		
		public int compareTo(ScheduledTask o) {
			if(this.equals(o)) return 0;

			BigDecimal forOrder = this.scheduledTime.subtract(o.scheduledTime);
			int order = forOrder.signum();
			if(order != 0) return order;
			order = System.identityHashCode(this) - System.identityHashCode(o);
			return order;
		}
	}
	
	public static void runTask(){
		if(taskQueue.size() > 0){
			ScheduledTask currentScheduledTask = taskQueue.poll();
			Task currentTask = currentScheduledTask.getTask();
			currentTime = currentScheduledTask.getScheduledTime();
			taskMap.remove(currentTask, currentScheduledTask);
			currentTask.run();
		}
	}

	public static void removeTask(Task task){
		if(taskMap.containsKey(task)){
			ScheduledTask stask = taskMap.get(task);
			taskQueue.remove(stask);
			taskMap.remove(task, stask);
		}
	}
	
	public static Task getTask(){
		if(taskQueue.size() > 0){
			ScheduledTask currentTask = taskQueue.peek();
			return currentTask.getTask();
		}else{
			return null;
		}
	}
	
	public static void putTask(Task task){
		ScheduledTask stask = new ScheduledTask(task, currentTime.add(task.getInterval()) );
		taskMap.put(task,stask);
		taskQueue.add(stask);
	}

	/**
	public static void putTaskAbsoluteTime(Task task,long time){
		ScheduledTask stask = new ScheduledTask(task, time);
		taskMap.put(task,stask);
		taskQueue.add(stask);
	}**/
	
	public static BigDecimal getCurrentTime(){return currentTime;}
}
