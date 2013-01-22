package hr.fer.hmo.projectscheduling.aco;

import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.Util;
import java.io.IOException;
import java.util.Random;

/**
 * Ant Colony Optimization
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 2.0
 */
public class ACO extends Algorithm {

	//rand
	private Random rand;

	//work unit indexes
	private int[] indexes;

	//pheromone trails
	private double[][] trails;

	//heursitics
	private double[][] heuristics;

	//ant colony
	private Ant[] ants;

	//availability
	private int[] reachable;

	//probabilities
	private double[] probabilities;

	//evaporation
	private double ro;

	//alpha constant
	private double α;

	//beta constant
	private double β;
	
	//initial pheromone trail
	private double initTrail;

	//colony size
	private int colonySize;

	//iteration or ant walk number
	private int walkNumber;

	//updating trails do 10% of best ants (or all ants)
	private boolean bestAntsMakeTrailUpdate = false;

	//best solution ever
	private Ant best;
	private boolean haveBest = false;

	//project info
	private int subProjectNumber;
	private int subProjectActivities;

	/**
	 * Constructor.
	 */
	public ACO() {

		//---------------------constants values---------------------
		α = 3;
		β = 0;
		ro = 0.2;
		colonySize = 50;
		walkNumber = 200;
		double firstInputSolutionValue = 1732.0;
		initTrail = colonySize/firstInputSolutionValue;

		//magic numbers!
		subProjectNumber = 15;
		subProjectActivities = 15;
		//------------------constants values end---------------------
	}

	/**
	 * The ACO algorithm.
	 */
	@Override
	public void run() {
		
		initialize();

		int iter = 0;

		while(iter < walkNumber) {
			iter++;

			for (int antIndex = 0; antIndex < ants.length; antIndex++) {
				Ant ant = ants[antIndex];
				doWalk(ant);
			}

			updateTrails();
			evaporateTrails();
			checkBestSolution();
			
			if (configuration != null) configuration.increase();

			//control print
			System.out.println(
					"Iter: " + iter + " Dur: " + best.getActualDuration()
					);

		}

		//best.calculateFitness();
		bestIndividual = (Individual) best;

		//control print
		System.out.println("ACO Best Project Duration: " + 
				bestIndividual.getActualDuration());

	}

	/**
	 * Set best individual of previous algorithm.
	 * NOT IMPORTANT FOR THIS ALGORITHM BUT CAN AVOID FILE
	 * READING.
	 */
	@Override
	public void setInitialIndividual(Individual initialIndividual) {

		//constructive algorithm; NO INITIAL INDIVIDUAL BUT CAN
		//AVOID FILE READING
		this.initialIndividual = initialIndividual;
		
		initialize();

	}

	//Initialization.
	private void initialize() {
		
		Ant initialAnt = null;
		
		try {
			
			if (this.initialIndividual != null) {
				
				initialAnt = 
						(Ant) new Ant(initialIndividual.getProjectWorkLists());
			
			} else {
				
				initialAnt = new Ant(Util.readInputFile("/HOM-project.txt"));
			
			}

		} catch (IOException e) { e.printStackTrace(); }

		//other initializations
		rand = new Random();
		indexes = new int[subProjectNumber*subProjectActivities];
		ACOUtil.linearFillArray(indexes);
		
		probabilities = new double[indexes.length];
		
		trails = new double[indexes.length][indexes.length];
		
		heuristics = new double[indexes.length][indexes.length];
		
		//only subprojectActivities visible
		reachable = new int[subProjectActivities]; 

		//setting trails and heuristics
		for (int i = 0; i < indexes.length; i++) {
			
			heuristics[i][i] = 0;
			trails[i][i] = initTrail;
			
			for (int j = i+1; j < indexes.length; j++) {
				
				trails[i][j] = initTrail;
				trails[j][i] = initTrail;

				//SOLVE HEURISTICS! β = 0, Simple ACO

				heuristics[i][j] = Math.pow(1, β);
				heuristics[j][i] = heuristics[i][j];
			
			}
		}

		ants = new Ant[colonySize];

		best = (Ant) initialAnt.clone();

		for (int i = 0; i < ants.length; i++) {
			ants[i] = (Ant) initialAnt.clone();
		}

	}

	/**
	 * Ant walk through graph.
	 * Building solution.
	 * @param ant
	 */
	private void doWalk(Ant ant) {

		//set reachable and shuffle it
		ACOUtil.changeReachable(reachable, 0);
		ACOUtil.shuffleArray(reachable, rand);
		ant.setWorkUnitIndex(0, reachable[0]);

		for (int step = 1; step < indexes.length; step++) {

			//1]
			int previousIndex = ant.getWorkUnitIndex(step-1);

			//2] compute probabilities for next step
			double probabilitySum = 0.0;
			for (int candidate = step%subProjectActivities; 
					candidate < reachable.length; candidate++) {

				int workIndex = reachable[candidate];

				probabilities[workIndex] =
						Math.pow(trails[previousIndex][workIndex], α) *
						heuristics[previousIndex][workIndex];
				probabilitySum += probabilities[workIndex];

			}

			//3] normalize probabilities
			for (int candidate = step%subProjectActivities; 
					candidate < reachable.length; candidate++) {
				int workIndex = reachable[candidate];
				probabilities[workIndex] = probabilities[workIndex] / probabilitySum;
			}

			//4] next step selection
			double number = rand.nextDouble();
			probabilitySum = 0.0;
			int selectedCandidate = -1;
			for (int candidate = step%subProjectActivities; 
					candidate < reachable.length; candidate++) {

				int workIndex = reachable[candidate];
				probabilitySum += probabilities[workIndex];

				if (number <= probabilitySum) {
					selectedCandidate = candidate;
					break;
				}

			}

			//5]
			if (selectedCandidate == -1) {
				selectedCandidate = reachable.length-1;
			}

			int tmp = reachable[step%subProjectActivities];
			reachable[step%subProjectActivities] = reachable[selectedCandidate];
			reachable[selectedCandidate] = tmp;
			ant.setWorkUnitIndex(step, reachable[step%subProjectActivities]);

			//6] goto activities of other subproject
			if ((step+1) % subProjectActivities == 0) {
				ACOUtil.changeReachable(reachable, step+1);
				ACOUtil.shuffleArray(reachable, rand);
			}
		}

		//evaluate ant solution
		ant.evaluate();
	}

	/**
	 * Update pheromone trails.
	 */
	private void updateTrails() {

		int updates = ants.length;

		if (bestAntsMakeTrailUpdate) {

			//first best 10% of ant colony
			updates = ants.length/10;
			ACOUtil.partialSort(ants, updates);

		}

		for (int antIndex = 0; antIndex < updates; antIndex++) {

			Ant ant = ants[antIndex];
			//currently only PROJECT_ACTUAL_DURATION
			double delta = 1.0 / ant.getActualDuration();
			for (int i = 0; i < indexes.length-1; i++) {

				int a = ant.getWorkUnitIndex(i);
				int b = ant.getWorkUnitIndex(i+1);
				trails[a][b] += delta;
				trails[b][a] = trails[a][b];

			}

		}  

	}

	/**
	 * Pheromone trails evaporation.
	 */
	private void evaporateTrails() {

		for (int i = 0; i < indexes.length; i++) {

			for (int j = i+1; j < indexes.length; j++) {
				trails[i][j] = trails[i][j]*(1-ro);
				trails[j][i] = trails[i][j];
			}

		}

	}

	/**
	 * Check the best solution.
	 */
	private void checkBestSolution() {

		if (!haveBest) {

			haveBest = true;
			Ant ant = ants[0];
			best.copyAnt(ant);

		}

		//currently only PROJECT_ACTUAL_DURATION
		int currentBest = best.getActualDuration();
		int bestIndex = -1;
		for (int antIndex = 0; antIndex < ants.length; antIndex++) {

			Ant ant = ants[antIndex];
			//currently only PROJECT_ACTUAL_DURATION
			if (ant.getActualDuration() < currentBest) {
				currentBest = ant.getActualDuration();
				bestIndex = antIndex;
			}

		}

		if (bestIndex != -1) {

			Ant ant = ants[bestIndex];
			best.copyAnt(ant);

		}
	}

	/**
	 * Algorithm name abbreviation.
	 */
	@Override
	public String toString() {

		return "ACO";

	}

	//------------------METHODS FOR GUI------------------
	//ABOVE METHODS GUI NAME

	//ρ
	//[0.0-1.0]
	public double getRo() {
		return ro;
	}

	public void setRo(double ro) {
		this.ro = ro;
	}

	//α
	//[0.0-3.0]
	public double getΑ() {
		return α;
	}

	public void setΑ(double α) {
		this.α = α;
	}

	//β
	//[0.0-3.0]
	public double getΒ() {
		return β;
	}

	public void setΒ(double β) {
		this.β = β;
	}

	//Colony size
	//[30-60]
	public int getColonySize() {
		return colonySize;
	}

	public void setColonySize(int colonySize) {
		this.colonySize = colonySize;
	}

	//Iterations
	//[150-300]
	public int getWalkNumber() {
		return walkNumber;
	}

	public void setWalkNumber(int walkNumber) {
		this.walkNumber = walkNumber;
	}

	//Best 10% make update (true/false)
	//false
	public boolean isBestAntsMakeTrailUpdate() {
		return bestAntsMakeTrailUpdate;
	}

	public void setBestAntsMakeTrailUpdate(boolean bestAntsMakeTrailUpdate) {
		this.bestAntsMakeTrailUpdate = bestAntsMakeTrailUpdate;
	}

	//----------------END METHODS FOR GUI-----------------
}