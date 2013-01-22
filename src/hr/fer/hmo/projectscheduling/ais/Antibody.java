package hr.fer.hmo.projectscheduling.ais;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.WorkUnit;

/**
 * Artificial Immune System - Clonal Selection Algorithm
 * Antibody
 * @author Ivo Majić, ivo.majic2@fer.hr
 * @version 1.0
 */
public class Antibody extends Individual {
	
	/**
	 * Constructor
	 * @param projectWorkLists list of projects
	 */
	public Antibody(List<ArrayList<WorkUnit>> projectWorkLists) {
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
	 * Switch positions of two random work units N times per project
	 * 
	 * @param mutations number of mutations per project
	 * @param rand random number genrator
	 */
	public void hyperMutate(int mutations, Random rand) {
		
		int projectSize = this.getProjectWorkLists().size();
		int mutationsDone = 0;
		
		while (mutationsDone < mutations) {
					
			int indexA = rand.nextInt(projectSize);
			int indexB = rand.nextInt(projectSize);
			
			if (indexA == indexB) {
				if (indexB == projectSize-1)
					indexB--;
				else
					indexB++;
			}
			
			ArrayList<WorkUnit> projectWorkList = projectWorkLists.get(
					rand.nextInt(projectSize)
			);
			
			WorkUnit temp = projectWorkList.get(indexA);
			projectWorkList.set(indexA, projectWorkList.get(indexB));
			projectWorkList.set(indexB, temp);
			mutationsDone++;
			
		}
		
	}

}
