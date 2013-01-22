package hr.fer.hmo.projectscheduling.pso;

import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.common.Individual;
import hr.fer.hmo.projectscheduling.common.Util;
import java.io.IOException;
import java.util.Random;

/**
 * Particle Swarm Optimization
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class PSO extends Algorithm {

	//max change of probability at once
	private double[] velBounds;

	//Percentage range search space that is used 
	//to calculate the displacement limits in one step
	private double velBoundsPercentage;

	//global iteration counter
	private int iteration;

	//weight on the start of the process
	private double linWeightStart;

	//weight of the end of the process
	private double linWeightEnd;

	//iteration in which weight falls to minimum
	private int linWeightTreshold;

	//swarm particle count
	private int swarmParticleCount;

	//random
	private Random rand;

	//c1 constant
	private double c1;

	//c2 constant
	private double c2;

	//increase or decrease velocity
	private double γ;

	//minimum value of probability (can be negative, but less then |-1|)
	private double probabilityMin;

	//maximum value of probability
	private double probabilityMax;

	//particles of swarm
	private Particle[] particles;

	//bes particle
	private Particle best;

	//neighborhood
	private Neighborhood neighborhood;
	
	//global neighborhood set
	private boolean globalNeighborhoodSet;

	//neighborhoodSize
	private int neighborhoodSize;

	//dmension of domain (subProjects*subProjectActivities)
	private int dimension;

	//number steps of algorithm
	private int swarmIterations;

	//have best solution?
	private boolean haveBest;


	/**
	 * Constructor.
	 */
	public PSO() {

		this(null);

	}

	/**
	 * Constructor.
	 * Get Best Individual From Previous Algorithm.
	 * @param bestIndividual or null
	 */
	public PSO(Individual bestIndividual) {

		PSOUtil.reset();
		
		//TEMPORARY-------------------------------------------------
		setInitialIndividual(bestIndividual);
		//TEMPORARY-------------------------------------------------

		//---------------------constants values---------------------
		swarmParticleCount = 30;
		swarmIterations = 300;

		c1 = 1;
		c2 = 5;

		iteration = 0;
		linWeightStart = 0.9;
		linWeightEnd = 0.4;
		linWeightTreshold = swarmIterations/2;

		//if γ<1 decreasing velocity
		γ = 0.5;

		probabilityMin = 0;
		probabilityMax = 1;
		velBoundsPercentage = 1;

		neighborhoodSize = 3;

		//magic number
		int subProjectNumber = 15;
		int subProjectActivities = 15;
		dimension = subProjectNumber * subProjectActivities;
		
		//by default: local neighborhood
		globalNeighborhoodSet = false;
		
		//------------------constants values end---------------------

	}

	/**
	 * The PSO Algorithm.
	 */
	@Override
	public void run() {
		
		initialize();

		for (int iter = 0; iter < swarmIterations; iter++) {

			checkBestSolution();

			makeSwarmMotion();
			
			if (configuration != null) configuration.increase();

			//control print
			System.out.println(
					"Iter: " + iter + " Dur: " + best.getBestProjectDuration()
					);
		}

		bestIndividual = (Individual) best;

		//control print
		System.out.println("PSO Best Project Duration: " + 
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
		
		PSOUtil.reset();

		Particle bestPreviousIndividual = this.initialIndividual!=null ?
				new Particle(this.initialIndividual.getProjectWorkLists()) : null;

		Particle initialParticle = null;
		
		try {
			
			if (bestPreviousIndividual != null) {
				
				initialParticle = (Particle)bestPreviousIndividual.clone();
			
			} else {
				
				initialParticle = new Particle(Util.readInputFile("/HOM-project.txt"));
			
			}
			
		} catch (IOException e) { e.printStackTrace(); }
		
		//magic number!
		int subProjectActivities = 15;
		
		velBounds = new double[dimension];
		

		for (int d = 0; d < dimension; d++) {
			velBounds[d] = (probabilityMax-probabilityMin)*velBoundsPercentage;
		}

		rand = new Random();

		particles = new Particle[swarmParticleCount];
		for (int i = 0; i < swarmParticleCount; i++) {

			particles[i] = (Particle) initialParticle.clone();

			//shuffle all particles (make initial population)
			PSOUtil.shuffleSubArray(
					particles[i].getProjectWorkUnitIndexes(), 
					subProjectActivities, 
					rand
					);

			if (i == 0) {
				particles[i] = bestPreviousIndividual!= null ? 
						bestPreviousIndividual : particles[i];
			}

			//evaluate swarm
			particles[i].evaluate();
			//set this value as best
			particles[i].setBestProjectDuration(
					particles[i].getActualDuration()
					);

		}
		
		//neighborhood: LOCAL OR GLOBAL NEIGHBORHOOD
		if (globalNeighborhoodSet) {
			neighborhood = new GlobalNeighborhood(dimension);
		} else {
			neighborhood = new LocalNeighborhood(swarmParticleCount, dimension, neighborhoodSize);
		}
		
		haveBest = false;

		best = (Particle) initialParticle.clone();
	}

	/**
	 * Swarm motion.
	 * Finds neighborhood best.
	 * Change velocitiy and paritcle position.
	 */
	private void makeSwarmMotion() {

		iteration++;

		double w;
		if (iteration > linWeightTreshold) {

			w = linWeightEnd;

		} else {

			w = linWeightStart + (linWeightEnd-linWeightStart)*
					(iteration-1.0)/linWeightTreshold;

		}

		neighborhood.scan(particles);

		for (int i = 0; i < particles.length; i++) {

			//find best neighborhood solution
			int[] socialBest = neighborhood.findBest(i);

			for (int d = 0; d < dimension; d++) {

				particles[i].setPreviousWorkUnitIndexes(
						d, particles[i].getProjectWorkUnitIndexes()[d]);

				//COMPUTE VELOCITY
				//IN THIS PROJECT VELOCITY = PROBABILITY OF VECTOR POSITION MUTATION

				double velocity = 
						w*particles[i].getVelocity()[d] +
						c1*rand.nextDouble() * (
								particles[i].getBestWorkUnitIndexes()[d] - 
								particles[i].getProjectWorkUnitIndexes()[d]
								) +
								c2*rand.nextDouble() * (
										socialBest[d] - 
										particles[i].getProjectWorkUnitIndexes()[d]
										);

				//increase or decrease velocity
				velocity *= γ;

				//1-Math.pow(Math.E, -Math.abs(velocity)) => velocity limited to interval [0,1]
				particles[i].setVelocity(d, 1-Math.pow(Math.E, -Math.abs(velocity)));

				if (particles[i].getVelocity()[d] < -velBounds[d]) {

					particles[i].setVelocity(d, -velBounds[d]);

				} else if (particles[i].getVelocity()[d] > velBounds[d]) {

					particles[i].setVelocity(d, velBounds[d]);
				}

			}

			//PARTICLE UPDATE POSITION
			particles[i].moveParticle(rand);

		}

		//set particle local best position
		for (int i = 0; i < particles.length; i++) {

			particles[i].evaluate();

			if (particles[i].getActualDuration() < 
					particles[i].getBestProjectDuration()) {

				particles[i].setBestProjectDuration(
						particles[i].getActualDuration()
						);

				particles[i].setBestWorkUnitIndexes(
						particles[i].getProjectWorkUnitIndexes()
						);

			}

		}

	}

	/**
	 * Check the best solution.
	 */
	private void checkBestSolution() {

		if (!haveBest) {

			haveBest = true;
			Particle particle = particles[0];
			best.copyParticle(particle);

		}

		//currently only PROJECT_ACTUAL_DURATION
		int currentBest = best.getBestProjectDuration();
		int bestIndex = -1;
		for (int particleIndex = 0; particleIndex < particles.length; particleIndex++) {

			Particle particle = particles[particleIndex];
			//currently only PROJECT_ACTUAL_DURATION
			if (particle.getBestProjectDuration() < currentBest) {
				currentBest = particle.getBestProjectDuration();
				bestIndex = particleIndex;
			}

		}

		if (bestIndex != -1) {

			Particle particle = particles[bestIndex];
			best.copyParticle(particle);

		}
	}

	/**
	 * Algorithm name abbreviation.
	 */
	@Override
	public String toString() {

		return "PSO";

	}

	//------------------METHODS FOR GUI------------------
	//ABOVE METHODS GUI NAME

	//Iterations
	//[200-400]
	public int getSwarmIterations() {
		return swarmIterations;
	}

	public void setSwarmIterations(int swarmIterations) {
		this.swarmIterations = swarmIterations;
	}

	//C1
	//[1-10]
	public double getC1() {
		return c1;
	}

	public void setC1(double c1) {
		this.c1 = c1;
	}

	//C2
	//[1-10]
	public double getC2() {
		return c2;
	}

	public void setC2(double c2) {
		this.c2 = c2;
	}

	//γ
	//[0.0-2.0]
	public double getΓ() {
		return γ;
	}

	public void setΓ(double γ) {
		this.γ = γ;
	}

	//Neighborhood Size
	//[1-7], step 2
	public int getNeighborhoodSize() {
		return neighborhoodSize;
	}

	public void setNeighborhoodSize(int neighborhoodSize) {
		this.neighborhoodSize = neighborhoodSize;
	}

	//Particle Count
	//[20-60]
	public int getSwarmParticleCount() {
		return swarmParticleCount;
	}

	public void setSwarmParticleCount(int swarmParticleCount) {
		this.swarmParticleCount = swarmParticleCount;
	}

	//by default false; by default local neighborhood
	public boolean isGlobalNeighborhoodSet() {
		return globalNeighborhoodSet;
	}

	public void setGlobalNeighborhoodSet(boolean globalNeighborhoodSet) {
		this.globalNeighborhoodSet = globalNeighborhoodSet;
	}
	
	//----------------END METHODS FOR GUI-----------------
}