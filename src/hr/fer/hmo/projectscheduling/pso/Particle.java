package hr.fer.hmo.projectscheduling.pso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.WorkUnit;

/**
 * PSOIndividual
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class Particle extends Individual {

	//best local solution
	private int[] bestWorkUnitIndexes;
	//value of best solution
	private int bestProjectDuration;

	//previous solution
	private int[] previousWorkUnitIndexes;

	//current solution
	//value of current solution stored in actualProjectDuration
	private int[] projectWorkUnitIndexes;

	//probability vector (can be negative but in range [-1,1]
	private double[] velocity;

	private Map<Integer, WorkUnit> indexes2WorkUnitsMap;

	/**
	 * Constructor.
	 * @param projectWorkLists
	 */
	public Particle(List<ArrayList<WorkUnit>> projectWorkLists) {

		super(projectWorkLists);

		indexes2WorkUnitsMap = PSOUtil.createIndexes2WorkUnitMap(projectWorkLists);

		projectWorkUnitIndexes = 
				new int[projectWorkLists.size()*projectWorkLists.get(0).size()];

		//linear fill particle
		//out of constructor recommended PSOUtil.shuffleSubArray
		PSOUtil.linearFillArray(projectWorkUnitIndexes);

		bestWorkUnitIndexes = new int[projectWorkUnitIndexes.length];

		previousWorkUnitIndexes = new int[projectWorkUnitIndexes.length];

		velocity = new double[projectWorkUnitIndexes.length];

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

	//getter
	public int getBestProjectDuration() {
		return bestProjectDuration;
	}

	//setter
	public void setBestWorkUnitIndexes(int[] bestWorkUnitIndexes) {

		System.arraycopy(
				bestWorkUnitIndexes, 
				0, 
				this.bestWorkUnitIndexes, 
				0, 
				bestWorkUnitIndexes.length
				);
	}

	//setter
	public void setBestProjectDuration(int bestProjectDuration) {
		this.bestProjectDuration = bestProjectDuration;
	}

	//getter
	public int[] getBestWorkUnitIndexes() {
		return bestWorkUnitIndexes;
	}

	//setter
	public void setPreviousWorkUnitIndexes(int index, int value) {
		this.previousWorkUnitIndexes[index] = value;
	}

	//getter
	public double[] getVelocity() {
		return velocity;
	}

	//setter
	public void setVelocity(int index, double value) {
		velocity[index] = value;
	}

	/**
	 * Particle moving.
	 * @param rand
	 */
	public void moveParticle(Random rand) {

		int[] copyProjectWorkUnitIndexes = 
				new int[projectWorkUnitIndexes.length];

		System.arraycopy(
				projectWorkUnitIndexes, 
				0, 
				copyProjectWorkUnitIndexes, 
				0, 
				projectWorkUnitIndexes.length
				);

		List<Integer> mutationIndexes = new ArrayList<Integer>();
		int cnt = 0;
		//magic number!
		int subProjectActivities = 15;
		for (int i = 0; i < velocity.length; i++) {
			
			cnt++;

			if (rand.nextDouble() <= Math.abs(velocity[i])) {
				mutationIndexes.add(i);
			}

			if (cnt == subProjectActivities) {
				
				cnt = 0;

				int[] arrayOfMutationIndexes = list2array(mutationIndexes);

				int[] copyOfArrayOfMutationIndexes = 
						new int[arrayOfMutationIndexes.length];

				System.arraycopy(
						arrayOfMutationIndexes, 
						0, 
						copyOfArrayOfMutationIndexes, 
						0, 
						arrayOfMutationIndexes.length
						);
				
				PSOUtil.shuffleSubArray(
						arrayOfMutationIndexes, 
						arrayOfMutationIndexes.length, 
						rand
						);
				
				for (int j = 0; j < arrayOfMutationIndexes.length; j++) {
					
					projectWorkUnitIndexes[copyOfArrayOfMutationIndexes[j]] =
							copyProjectWorkUnitIndexes[arrayOfMutationIndexes[j]];
					
				}
	
				mutationIndexes.clear();
			
			}

		}

	}

	//converting list to array
	private int[] list2array(List<Integer> mutationIndexes) {

		Integer[] arrayOfMutationIndexesObjects = 
				mutationIndexes.toArray(new Integer[mutationIndexes.size()]);

		int[] arrayOfMutationIndexes = 
				new int[arrayOfMutationIndexesObjects.length];

		for (int i = 0; i < arrayOfMutationIndexesObjects.length; i++) {
			arrayOfMutationIndexes[i] = 
					arrayOfMutationIndexesObjects[i].intValue();
		}

		return arrayOfMutationIndexes;

	}

	//Copy method
	public void copyParticle(Particle particle) {

		System.arraycopy(particle.projectWorkUnitIndexes, 0, 
				this.projectWorkUnitIndexes, 0, 
				particle.projectWorkUnitIndexes.length);
		
		System.arraycopy(particle.bestWorkUnitIndexes, 0, 
				this.bestWorkUnitIndexes, 0, 
				particle.bestWorkUnitIndexes.length);
		
		System.arraycopy(particle.previousWorkUnitIndexes, 0, 
				this.previousWorkUnitIndexes, 0, 
				particle.previousWorkUnitIndexes.length);
		
		System.arraycopy(particle.velocity, 0, 
				this.velocity, 0, 
				particle.velocity.length);
		
		this.bestProjectDuration = particle.bestProjectDuration;
		
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