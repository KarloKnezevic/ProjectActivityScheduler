package hr.fer.hmo.projectscheduling.eda;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.WorkUnit;

/**
 * Eda Individual.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class EdaChromosome extends Individual {

	//indexes solution
	private int[] projectWorkUnitIndexes;

	//map for indexes to work units transforming
	private Map<Integer, WorkUnit> indexes2WorkUnitsMap;

	/**
	 * Constructor.
	 * @param projectWorkLists
	 */
	public EdaChromosome(List<ArrayList<WorkUnit>> projectWorkLists) {

		super(projectWorkLists);

		indexes2WorkUnitsMap = 
				EDAUtil.createIndexes2WorkUnitMap(projectWorkLists);

		projectWorkUnitIndexes = 
				new int[projectWorkLists.size()*projectWorkLists.get(0).size()];

		EDAUtil.linearFillArray(projectWorkUnitIndexes);

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

	//getter
	public int[] getProjectWorkUnitIndexes() {
		return projectWorkUnitIndexes;
	}
	
	/**
	 * SAMPLING FROM DISTRIBUTION.
	 * THE MAIN METHOD OF EDA.
	 */
	public void distributionSample() {
		
		int[] newProjectWorkUnitIndexes = 
				//sampling
				EDAUtil.multivariateUniqueDistribution();
		
		System.arraycopy(
				newProjectWorkUnitIndexes, 
				0, 
				projectWorkUnitIndexes, 
				0, 
				projectWorkUnitIndexes.length
				);
		
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