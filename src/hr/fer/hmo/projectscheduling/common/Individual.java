package hr.fer.hmo.projectscheduling.common;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Individual
 * @author Karlo Knežević, karlo.knezevic@fer
 * @author Ivo Majić, ivo.majic2@fer.hr
 * @version 1.0
 */
public class Individual implements Comparable<Individual>, Cloneable {

	protected List<ArrayList<WorkUnit>> projectWorkLists;

	protected List<TreeSet<WorkUnitInterval>> personWorkLists;
	protected int actualDuration, waitDuration;

	public Individual(List<ArrayList<WorkUnit>> projectWorkLists) {

		this.projectWorkLists = projectWorkLists;
		this.actualDuration = Integer.MAX_VALUE;
		this.waitDuration = Integer.MAX_VALUE;

	};

	@SuppressWarnings("unused")
	private Individual() {}

	public void calculateFitness() {

		this.personWorkLists = new ArrayList<TreeSet<WorkUnitInterval>>();
		List<TreeSet<WorkUnit>> unlockedPersonWorkLists = 
				new ArrayList<TreeSet<WorkUnit>>();

		int projectCount = projectWorkLists.size();
		int workUnitCount = projectCount * projectCount;
		boolean[] workUnitAdded = new boolean[projectCount];

		int timeOfInterest = 0;
		int finishedWorkUnits = 0;
		
		// Initialize sets and lists
		for (int index = 0; index < projectCount; index++) {
			personWorkLists.add(new TreeSet<WorkUnitInterval>());
			unlockedPersonWorkLists.add(new TreeSet<WorkUnit>());
		}
		
		// Add initially unlocked work units to the unlocked list
		for (ArrayList<WorkUnit> projectWorkList : projectWorkLists) {
			
			WorkUnit workUnit = projectWorkList.get(0);
			TreeSet<WorkUnit> unlockedPersonWorkList = 
					unlockedPersonWorkLists.get(workUnit.getWorkerId());
			unlockedPersonWorkList.add(workUnit);
			
		}

		// Repeat until all work units are done
		while (finishedWorkUnits < workUnitCount) {
			
			// Go trough all currently active work units and find the one with
			// the lowest end time, thats the new time of interest
			int minEndTime = Integer.MAX_VALUE;
			for (TreeSet<WorkUnitInterval> personWorkList : personWorkLists) {

				if (!personWorkList.isEmpty()) {
					WorkUnitInterval workUnit = personWorkList.last();
					int workUnitEndTime = workUnit.getEndTime();
					if (workUnitEndTime < minEndTime && !workUnit.isFinished()) 
						minEndTime = workUnitEndTime;
				}

			}
			
			for (TreeSet<WorkUnitInterval> personWorkList : personWorkLists) {

				if (!personWorkList.isEmpty()) {
					WorkUnitInterval workUnit = personWorkList.last();
					int workUnitEndTime = workUnit.getEndTime();
					if (workUnitEndTime == minEndTime) {
						workUnit.setIsFinished();
						finishedWorkUnits++;
					}
				}

			}

			if (minEndTime != Integer.MAX_VALUE)
				timeOfInterest = minEndTime;
			
			int personId = 0;
			for (TreeSet<WorkUnitInterval> personWorkList : personWorkLists) {
				
				int endTime = 0;
				if (!personWorkList.isEmpty()) {
					endTime = personWorkList.last().getEndTime();
				}
				
				if (timeOfInterest >= endTime) {
					
					if (!personWorkList.isEmpty()) {
						
						WorkUnit lastWorkUnit = 
								personWorkList.last().getWorkUnit();
						List<WorkUnit> projectWorkList = 
								projectWorkLists.get(
										lastWorkUnit.getProjectId()
								);
						int lastWorkUnitId = 
								projectWorkList.indexOf(lastWorkUnit);
						
						if (lastWorkUnitId+1 < projectWorkList.size()) {
							
							WorkUnit nextWorkUnit = 
									projectWorkList.get(lastWorkUnitId+1);
							
							if (!workUnitAdded[personId]) {
								
								unlockedPersonWorkLists.get(
										nextWorkUnit.getWorkerId()
								).add(nextWorkUnit);
								workUnitAdded[personId] = true;
								
							}
							
						}
						
					}
					
				}
				
				personId++;
				
			}
			
			personId = 0;
			for (TreeSet<WorkUnitInterval> personWorkList : personWorkLists) {
				
				int endTime = 0;
				if (!personWorkList.isEmpty()) {
					endTime = personWorkList.last().getEndTime();
				}
				
				if (timeOfInterest >= endTime) {
					
					TreeSet<WorkUnit> unlockedPersonList = 
							unlockedPersonWorkLists.get(personId);
					
					if (!unlockedPersonList.isEmpty()) {
						
						WorkUnit workUnit = unlockedPersonList.first();
						personWorkList.add(
								new WorkUnitInterval(workUnit, timeOfInterest)
						);
						workUnitAdded[personId] = false;
						unlockedPersonList.remove(workUnit);
						
					}
					
				}
				
				personId++;
				
			}

		}
		
		this.actualDuration = 0;
		this.waitDuration = 0;

		for (TreeSet<WorkUnitInterval> personWorkList : personWorkLists) {

			if (personWorkList.isEmpty()) continue;

			int lastEndTime = 0;
			for (WorkUnitInterval workUnit : personWorkList) {
				
				this.waitDuration = 
					this.waitDuration + workUnit.getStartTime() - lastEndTime; 
				lastEndTime = workUnit.getEndTime();
				
			}

			int endTime = personWorkList.last().getEndTime();
			if (endTime > this.actualDuration) 
				this.actualDuration = endTime;

		}

	}

	@Override
	public int compareTo(Individual other) {

		if (this.actualDuration > other.actualDuration) return 1;
		if (this.actualDuration < other.actualDuration) return -1;

		if (this.waitDuration > other.waitDuration) return 1;
		if (this.waitDuration < other.waitDuration) return -1;
		
		return 1;

	}

	public int getActualDuration() {
		return actualDuration;
	}

	public List<ArrayList<WorkUnit>> getProjectWorkLists() {
		return projectWorkLists;
	}
	
	public int getWaitDuration() {
		return waitDuration;
	}

	public void writeInputFile(String fileName) throws IOException {
		
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(
						new BufferedOutputStream(
								new FileOutputStream(fileName)),"UTF-8"));
		
		int projectId = 0;
		for (ArrayList<WorkUnit> projectWorkList : this.projectWorkLists) {

			StringBuilder builder = new StringBuilder();
			builder.append(projectId);
			if (projectId < 10) builder.append(" :"); else builder.append(":");
			for (WorkUnit workUnit : projectWorkList) {
				builder.append(workUnit);
			}

			writer.write(builder.toString());
			writer.newLine();

			projectId++;

		}

		writer.close();
		
	}

	public void writeOutputFile(String fileName) throws IOException {
		
		this.calculateFitness();

		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(
						new BufferedOutputStream(
								new FileOutputStream(fileName)),"UTF-8"));

		writer.write(String.valueOf(this.getActualDuration()));
		writer.newLine();
		writer.newLine();

		int personId = 0;
		for (TreeSet<WorkUnitInterval> personWorkList : this.personWorkLists) {

			StringBuilder builder = new StringBuilder();
			builder.append(personId);
			builder.append(": ");
			for (WorkUnitInterval workUnit : personWorkList) {
				builder.append(workUnit);
				builder.append("-");
			}

			builder.deleteCharAt(builder.length()-1);
			writer.write(builder.toString());
			writer.newLine();
			writer.newLine();

			personId++;

		}

		writer.close();

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Individual clone() {
		
		Class currentClass = this.getClass();
		List<ArrayList<WorkUnit>> clonedProjectWorkLists = 
				new ArrayList<ArrayList<WorkUnit>>();
		
		for (ArrayList<WorkUnit> projectWorkList : this.getProjectWorkLists()) {
			
			ArrayList<WorkUnit> clonedProjectWorkList = 
					new ArrayList<WorkUnit>();
			for (WorkUnit workUnit : projectWorkList) {
				clonedProjectWorkList.add(workUnit);
			}
			clonedProjectWorkLists.add(clonedProjectWorkList);
			
		}
		
		try {
			return (Individual) currentClass.getConstructor(List.class)
									.newInstance(clonedProjectWorkLists);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	protected class WorkUnitInterval implements Comparable<WorkUnitInterval> {

		private WorkUnit workUnit;
		private int startTime;
		private int endTime;
		
		private boolean finished;

		public WorkUnitInterval(WorkUnit workUnit, int startTime) {
			this.workUnit = workUnit;
			this.startTime = startTime;
			this.endTime = startTime + workUnit.getWorkLoad();
			this.finished = false;
		}

		@Override
		public int compareTo(WorkUnitInterval other) {

			if (this.endTime < other.startTime) return -1;
			if (this.endTime > other.startTime) return 1;

			if (this.startTime < other.endTime) return -1;
			return 1;

		}

		public int getEndTime() {
			return endTime;
		}

		public int getStartTime() {
			return startTime;
		}

		public WorkUnit getWorkUnit() {
			return workUnit;
		}

		public boolean isFinished() {
			return finished;
		}

		public void setIsFinished() {
			this.finished = true;
		}

		@Override
		public String toString() {
			return workUnit.getProjectId()+"("+startTime+";"+endTime+")";
		}

	}

}
