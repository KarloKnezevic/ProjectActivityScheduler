package hr.fer.hmo.projectscheduling.eda;

import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Estimation Of Distribution Algorithm.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class EDA extends Algorithm {

	//population size
	private int populationSize;

	//proportion for estimation, [0-1]
	private double individualsProportionForEstimation;

	//iterations
	private int generations;

	//population
	private EdaChromosome[] population;

	//selected individuals
	private EdaChromosome[] selectedIndividuals;

	//smoothing constant
	private double λ;

	//random
	private Random rand;

	//selection
	private ISelection selection;

	private int dimension;

	/**
	 * Constructor.
	 */
	public EDA() {

		this(null);

	}

	/**
	 * Constructor.
	 * Get best individual of previous algorithm.
	 * @param bestIndividual
	 */
	public EDA(Individual bestIndividual) {

		//reset utility
		EDAUtil.reset();
		
		//TEMPORARY-------------------------------------------------
		setInitialIndividual(bestIndividual);
		//TEMPORARY-------------------------------------------------

		//---------------------constants values---------------------
		populationSize = 30;
		individualsProportionForEstimation = 0.7;
		generations = 300;
		//BEST FOR 0!!!
		λ = 0;
		EDAUtil.setLambda(λ);

		selection = new FirstNBestSelection();
		//selection = new ProportionalSelection();

		//magic number
		int subProjectNumber = 15;
		int subProjectActivities = 15;
		dimension = subProjectNumber * subProjectActivities;
		//------------------constants values end---------------------

	}

	/**
	 * The EDA Algorithm.
	 */
	@Override
	public void run() {
		
		initialize();

		//generation algorithm
		//ELITISM -> the best individual (0.) transfered to the new generation
		for (int generation = 0; generation < generations; generation++) {

			//sort population
			Arrays.sort(population);

			//control print
			System.out.println(
					"Iter: " + generation + " Dur: " + population[0].getActualDuration());

			//select individuals
			selection.select(population, selectedIndividuals);

			//estimate distribution parameters
			estimateProbabilitiesNewGenes();

			//sample new individuals from distribution
			createNewPopulation();

			//evaluate new population
			populationEvaluate();
			
			if (configuration != null) configuration.increase();

		}

		Arrays.sort(population);

		bestIndividual = (Individual) population[0];

		//control print
		System.out.println("EDA Best Project Duration: " + 
				bestIndividual.getActualDuration());

	}

	/**
	 * Set best individual of previous algorithm.
	 */
	@Override
	public void setInitialIndividual(Individual initialIndividual) {

		this.initialIndividual = initialIndividual;
		
		initialize();

	}

	//initialization
	private void initialize() {

		EDAUtil.reset();

		EdaChromosome bestPreviousIndividual = this.initialIndividual!=null ?
				new EdaChromosome(this.initialIndividual.getProjectWorkLists()) : null;

		EdaChromosome initialEdaChromosome = null;
		
		try {
			
			if (bestPreviousIndividual == null) {
				
				initialEdaChromosome = new EdaChromosome(
						Util.readInputFile("/HOM-project.txt")
						);
			
			} else {
				
				initialEdaChromosome = (EdaChromosome)bestPreviousIndividual.clone();
				
			}
			
		} catch (IOException e) { e.printStackTrace(); }

		//magic number!
		int subProjectActivities = 15;
		
		rand = new Random();

		//sync random 
		EDAUtil.setRand(rand);
		population = new EdaChromosome[populationSize];
		selectedIndividuals = new EdaChromosome[(int)Math.ceil(
				populationSize*individualsProportionForEstimation)];

		//create population
		for (int i = 0; i < populationSize; i++) {

			population[i] = (EdaChromosome)initialEdaChromosome.clone();

			EDAUtil.shuffleSubArray(
					population[i].getProjectWorkUnitIndexes(), 
					subProjectActivities
					);

			if (i == 0) {

				population[i] = bestPreviousIndividual!= null ? 
						bestPreviousIndividual : population[i];

			}

			population[i].evaluate();
		}

	}

	/**
	 * Detecting the number of samples for parameters learning.
	 */
	private void estimateProbabilitiesNewGenes() {

		List<Map<Integer, Integer>> statisticList = 
				new ArrayList<Map<Integer, Integer>>();

		for (int i = 0; i < dimension; i++) {

			Map<Integer, Integer> iThGeneCnt = new HashMap<Integer, Integer>();

			for (int j = 0; j < selectedIndividuals.length; j++) {

				int key = selectedIndividuals[j].getProjectWorkUnitIndexes()[i];

				if (iThGeneCnt.containsKey(key)) {
					iThGeneCnt.put(key, iThGeneCnt.get(key)+1);
				} else {
					iThGeneCnt.put(key, 1);
				}

			}

			statisticList.add(iThGeneCnt);

		}

		//set statistical list
		EDAUtil.setStatisticList(statisticList);

	}

	/**
	 * New population creating.
	 */
	private void createNewPopulation() {

		//0. is best individual - elitism
		for (int i = 1; i < populationSize; i++) {
			population[i].distributionSample();
		}

	}

	/**
	 * Evaluation.
	 */
	private void populationEvaluate() {

		for (int i = 0; i < populationSize; i++) {
			population[i].evaluate();
		}

	}

	/**
	 * Algorithm name abbreviation.
	 */
	@Override
	public String toString() {

		return "EDA";

	}

	//------------------METHODS FOR GUI------------------
	//ABOVE METHODS GUI NAME

	//Individuals for ε
	//[0.0 - 1.0]
	public double getIndividualsProportionForEstimation() {
		return individualsProportionForEstimation;
	}

	public void setIndividualsProportionForEstimation(
			double individualsProportionForEstimation) {
		this.individualsProportionForEstimation = individualsProportionForEstimation;
	}

	//Iterations
	//[100 - 300]
	public int getGenerations() {
		return generations;
	}

	public void setGenerations(int generations) {
		this.generations = generations;
	}

	//Population size
	//[20-50]
	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}
	
	//λ
	//[0 - 10]
	public double getΛ() {
		return λ;
	}

	public void setΛ(double λ) {
		this.λ = λ;
		EDAUtil.setLambda(λ);
	}

	//----------------END METHODS FOR GUI-----------------
}