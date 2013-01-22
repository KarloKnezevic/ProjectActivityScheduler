package hr.fer.hmo.projectscheduling.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.WorkUnit;

/**
 * Genetic Algorithm
 * Chromosome
 * @author Petar Čolić, petar.colic@fer.hr
 * @version 1.0
 */
public class Chromosome extends Individual {

	public Chromosome(List<ArrayList<WorkUnit>> projectWorkLists) {
		super(projectWorkLists);
	}
	
	
	/**
	 * Randomize all projects
	 */
	public void randomizeAll() {
		
		for (ArrayList<WorkUnit> projectWorkList : projectWorkLists) {
			Collections.shuffle(projectWorkList);
		}
		
	}
	
	
	/**
	 * Mutation
	 * @param lineProb Probability for a mutation in a single line
	 * @param rand
	 */
	public void mutate(double lineProb, Random rand) {

		int projectSize = this.getProjectWorkLists().size();

		for (ArrayList<WorkUnit> projectWorkList : projectWorkLists) {

			if (rand.nextDouble() < lineProb) {

				int indexA = rand.nextInt(projectSize);
				int indexB = rand.nextInt(projectSize);

				if (indexA == indexB) {
					if (indexB == projectSize - 1)
						indexB--;
					else
						indexB++;
				}

				WorkUnit temp = projectWorkList.get(indexA);
				projectWorkList.set(indexA, projectWorkList.get(indexB));
				projectWorkList.set(indexB, temp);
			}

		}

	}

}
