package hr.fer.hmo.projectscheduling.ga;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.Util;

/**
 * Genetic Algorithm
 * @author Petar Čolić, petar.colic@fer.hr
 * @version 1.0
 */
public class GA extends Algorithm {
	
	// population size parameter
	private int popsize;

	// Number of "elite" chromosomes
	private int elitism;

	// Mutation probability
	private double mutProb;

	// Crossover probability
	private double crossProb;

	// Population
	private Chromosome[] population;

	// New generation
	private Chromosome[] newGeneration;

	// Number of generations
	private int nGenerations;

	// Random number generator
	private Random rand;

	public GA() {
		
		popsize=200;
		elitism=1;
		mutProb=0.02;
		crossProb=0.8;
		nGenerations=500;
		
	}

	@Override
	public void run() {

		initialize();
				
		newGeneration = new Chromosome[popsize];

		for (int k = 0; k < nGenerations; k++) {
			
			evaluate(population);
			Arrays.sort(population);
			
			System.out.println(
				"Iter: " + k + " Dur: " + population[0].getActualDuration()
			);

			// Elitism
			for (int i = 0; i < elitism; i++)
				newGeneration[i] = (Chromosome) population[i].clone();

			for (int i = elitism; i < popsize; i++) {
				
				Chromosome child = crossover(tourPick(), randomChoice());
				mutate(child);
				newGeneration[i] = child;
				
			}

			Chromosome[] pom = population;
			population = newGeneration;
			newGeneration = pom;
			
			if (configuration != null) configuration.increase();

		}
		
		evaluate(population);
		Arrays.sort(population);
		
		this.bestIndividual = (Individual) population[0];
		System.out.println("GA Best Project Duration: " + 
				this.bestIndividual.getActualDuration()
		);
		
		
	}
	
	/**
	 * Mutation where mutProb is mutation prob. for each line individualy
	 * @param child
	 */
	private void mutate(Chromosome child) {
		
		child.mutate(mutProb, rand);
		
	}

	/**
	 * Crossover with a single crosspoint
	 * @param parent1 first parent chromosome
	 * @param parent2 second parent chromosome
	 * @return child child chromosome
	 */
	private Chromosome crossover(Chromosome parent1, Chromosome parent2) {
		
		Chromosome child;
		if (rand.nextDouble() > crossProb) {
			
			int cmpr = parent1.compareTo(parent2);
			
			if (cmpr < 0)
				child = (Chromosome) parent1.clone();
			else
				child = (Chromosome) parent2.clone();

		} else {

			int length = parent1.getProjectWorkLists().size();
			int crosspoint = rand.nextInt(length-2)+1;
			child = (Chromosome) parent1.clone();
			
			for (int index = crosspoint; index < length; index++){
				
				child.getProjectWorkLists().set(
						index, 
						parent2.clone().getProjectWorkLists().get(index)
				);
				
			}
			
		}
		
		return child;

	}

	/**
	 * Select 3 individuals, use the best one
	 * @return
	 */
	private Chromosome tourPick() {
		return population[Math.min(
			Math.min(rand.nextInt(popsize), rand.nextInt(popsize)), 
			rand.nextInt(popsize))];
	}

	/**
	 * Randomly select an individual
	 * @return
	 */
	private Chromosome randomChoice() {
		return population[rand.nextInt(popsize)];
	}

	/**
	 * Evaluate the population
	 * @param population population to be evaluated
	 */
	private void evaluate(Chromosome[] population) {
		
		for (int index = 0; index < population.length; index++) {
			population[index].calculateFitness();
		}
		
	}

	/**
	 * Initializes parameters and starting population
	 */
	private void initialize() {
		
		rand = new Random();
		population = new Chromosome[popsize];
		
		try {
			
			Chromosome initialChromosome;
			if (initialIndividual == null) {
				
				initialChromosome = new Chromosome(
										Util.readInputFile(
											"/HOM-project.txt"
										)
									);
				
			} else {
				
				initialChromosome = new Chromosome(
						initialIndividual.clone().getProjectWorkLists()
				);
				
			}
			
			population[0] = initialChromosome;
			for(int i = 1; i < popsize; i++) {
				
				population[i] = (Chromosome) initialChromosome.clone();
				population[i].randomizeAll();
			
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String toString() {
		return "GA";
	}

	@Override
	public void setInitialIndividual(Individual initialIndividual) {
		this.initialIndividual = initialIndividual;
		initialize();
	}
	
	// ------------------METHODS FOR GUI------------------
	// ABOVE METHODS GUI NAME
	
	// Number of generations
	// [100-2000]
	public int getIterations() {
		return nGenerations;
	}

	public void setIterations(int nGenerations) {
		this.nGenerations = nGenerations;
	}
	
	// Population size
	// [50-2000]
	public void setPopsize(int popsize) {
		this.popsize = popsize;
	}
	
	public int getPopsize() {
		return popsize;
	}
	
	// Elitism
	// [0-5]
	public int getElitism() {
		return elitism;
	}
	
	public void setElitism(int elitism) {
		this.elitism = elitism;
	}
	
	// Crossover probability
	// [0.1-1.00]
	public void setCrossProb(double crossProb) {
		this.crossProb = crossProb;
	}
	
	public double getCrossProb() {
		return crossProb;
	}
	
	// Mutation probability
	// [0.0001-0.05]
	public void setMutProb(double mutProb) {
		this.mutProb = mutProb;
	}
	
	public double getMutProb() {
		return mutProb;
	}
	
	
	// ----------------END METHODS FOR GUI-----------------
	
}
