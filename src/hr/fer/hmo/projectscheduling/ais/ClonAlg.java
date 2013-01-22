package hr.fer.hmo.projectscheduling.ais;

import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * Artificial Immune System - Clonal Selection Algorithm
 * @author Ivo Majić, ivo.majic2@fer.hr
 * @version 1.0
 */
public class ClonAlg extends Algorithm {
	
	// Cloned population size parameter
	private int paramβ;
	
	// Random antibodies per iteration
	private int paramD;
	
	// Population size
	private int paramN;
	
	// Iterations
	private int iterations;
	
	// Population
	private Antibody[] population;
	
	// Cloned population
	private Antibody[] clonedPopulation;
	
	// Cloned population ranks
	private int[] clonedPopulationRanks;
	
	// Cloned population size
	private int clonedPopulationSize;
	
	// Random number generator
	private Random rand;
	
	/**
	 * Constructor
	 */
	public ClonAlg() {
		
		paramN = 100;
		paramD = 10;
		paramβ = 1;
		iterations = 50;
		
	}
	
	public ClonAlg(Individual bestIndividual) {
		
		this();
		setInitialIndividual(bestIndividual);
		
	}

	/**
	 * The Clonal Selection Algorithm
	 */
	public void run() {
		
		int iter = 0;
		initialize();
		
		while (iter < iterations) {
			
			iter++;
			affinity(population);
			cloning();
			hyperMutation();
			affinity(clonedPopulation);
			select();
			birthAndReplace();
			System.out.println(
				"Iter: " + iter + " Dur: " + population[0].getActualDuration()
			);
			
			if (configuration != null) configuration.increase();
			
		}
		
		this.bestIndividual = (Individual) population[0];
		System.out.println("ClonAlg Best Project Duration: " + 
				this.bestIndividual.getActualDuration()
		);
		
	}
	
	/**
	 * Initialize algorithm parameters and population arrays, also generates 
	 * initial population of random antibodies
	 */
	private void initialize() {
		
		rand = new Random();
		population = new Antibody[paramN];
		
		clonedPopulationSize = 0;
		for(int i = 1; i <= paramN; i++) 
			clonedPopulationSize += (int)((paramβ*paramN)/((double) i)+0.5);
		
		clonedPopulation = new Antibody[clonedPopulationSize];
		clonedPopulationRanks = new int[clonedPopulationSize];
		
		try {
			
			Antibody initialAnitbody;
			if (initialIndividual == null) {
				
				initialAnitbody = 	new Antibody(
										Util.readInputFile(
											"/HOM-project.txt"
										)
									);
				
			} else {
				
				initialAnitbody = new Antibody(
						initialIndividual.clone().getProjectWorkLists()
				);
				
			}
			
			population[0] = initialAnitbody;
			for(int i = 1; i < paramN; i++) {
				
				population[i] = (Antibody) initialAnitbody.clone();
				population[i].randomizeAll();
			
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Calculate affinity (fitness) of all antibodies
	 * @param population the population to be evaluated
	 */
	private void affinity(Antibody[] population) {
		
		for (int index = 0; index < population.length; index++) {
			population[index].calculateFitness();
		}
		
	}
	
	/**
	 * Proportional cloning of antibodies in the population, better antibodies 
	 * get more clones
	 */
	private void cloning() {
		
		Arrays.sort(population);
		int index = 0;
		for (int rank = 1; rank <= population.length; rank++) {
			
			int copies = (int)((paramβ*paramN)/((double) rank) + 0.5);
			for (int copy = 0; copy < copies; copy++) {
				clonedPopulation[index] = (Antibody) population[rank-1].clone();
				clonedPopulationRanks[index] = rank;
				index++;
			}
			
		}
		
	}
	
	/**
	 * Mutate cloned population antibodies where better antibodies are 
	 * mutated less
	 */
	private void hyperMutation() {
		
		double tau = 3.476 * (population.length-1);
		// We leave the best individual intact, so we begin with 1
		for (int index = 1; index < clonedPopulation.length; index++) {
			Antibody currentAntibody = clonedPopulation[index];
			int rank = clonedPopulationRanks[index]-1;
			int mutations = (int)(1+225*0.25* (1-Math.exp(-rank/tau))+0.5);
			currentAntibody.hyperMutate(mutations, rand);
		}
		
	}
	
	/**
	 * Selects N best individuals from the cloned population. N is the normal 
	 * population size.
	 */
	private void select() {
		
		Arrays.sort(clonedPopulation);
		for (int index = 0; index < population.length; index++) {
			population[index] = clonedPopulation[index];
		}
		
	}	
	
	/**
	 * Replaces paramD worst antibodies in the population with randomly 
	 * generated ones
	 */
	private void birthAndReplace() {
		
		int offset = population.length-paramD;
		for (int index = 0; index < paramD; index++) {
			population[offset+index].randomizeAll();
		}
		
	}

	/**
	 * Set best individual of previous algorithm if chained
	 */
	@Override
	public void setInitialIndividual(Individual initialIndividual) {
		
		this.initialIndividual = initialIndividual;
		initialize();
		
	}
	
	/**
	 * Algorithm name abbreviation.
	 */
	@Override
	public String toString() {
		return "ClonAlg";
	}
	
	// ------------------METHODS FOR GUI------------------
	// ABOVE METHODS GUI NAME
	
	// Iterations
	// [25-200]
	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}
	
	// β
	// [1-50]
	public int getParamβ() {
		return paramβ;
	}
	
	public void setParamβ(int paramβ) {
		this.paramβ = paramβ;
		initialize();
	}
	
	// Random antibodies per iteration
	// [10-100]
	public int getParamD() {
		return paramD;
	}
	
	public void setParamD(int paramD) {
		this.paramD = paramD;
	}

	// Population size
	// [50-500]
	public int getParamN() {
		return paramN;
	}
	
	public void setParamN(int paramN) {
		this.paramN = paramN;
		initialize();
	}
	
	// ----------------END METHODS FOR GUI-----------------

}
