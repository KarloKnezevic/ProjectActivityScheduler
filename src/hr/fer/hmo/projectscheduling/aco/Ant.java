package hr.fer.hmo.projectscheduling.aco;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.WorkUnit;

/**
 * ACO Individual.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 2.0
 */
public class Ant extends Individual {

	private int[] projectWorkUnitIndexes;
	private Map<Integer, WorkUnit> indexes2WorkUnitsMap;

	//Constructor.
	public Ant(List<ArrayList<WorkUnit>> projectWorkLists) {

		super(projectWorkLists);

		indexes2WorkUnitsMap = ACOUtil.createIndexes2WorkUnitMap(projectWorkLists);
		
		projectWorkUnitIndexes = 
				new int[projectWorkLists.size()*projectWorkLists.get(0).size()];

	}


	//Recreate Individual from Indexes
	private void createIndividualFromIndexes() {

		int subProjectActivities = projectWorkLists.get(0).size();
		for (int i = 0; i < projectWorkLists.size(); i++) {
			
			ArrayList<WorkUnit> workUnitList = projectWorkLists.get(i);
			for (int j = 0; j < subProjectActivities; j++) {
				workUnitList.set(j, indexes2WorkUnitsMap.get(
						Integer.valueOf(
								projectWorkUnitIndexes[i*subProjectActivities+j]
										)
								)
						);
			}
			projectWorkLists.set(i, workUnitList);
		
		}

	}

	//Set Work Unit Index
	public void setWorkUnitIndex(int index, int value) {

		projectWorkUnitIndexes[index] = value;

	}

	//Get Work Unit Index
	public int getWorkUnitIndex(int index) {

		return projectWorkUnitIndexes[index];

	}

	//Copy method
	public void copyAnt(Ant ant) {
		
		System.arraycopy(ant.projectWorkUnitIndexes, 0, 
				this.projectWorkUnitIndexes, 0, 
				ant.projectWorkUnitIndexes.length);
		evaluate();

	}

	/**
	 * Calculate Fitness
	 * IMPORTANT: before calculateFitness execution, 
	 * createIndividualFromIndexes must be invoked!
	 */
	public void evaluate() {

		createIndividualFromIndexes();  
		calculateFitness();

	}
}