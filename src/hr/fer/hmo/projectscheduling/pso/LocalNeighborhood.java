package hr.fer.hmo.projectscheduling.pso;

/**
 * Local Neighborhood.
 * Particles are aware of the best solutios in 
 * their own neighborhood.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class LocalNeighborhood implements Neighborhood {

	//dimension of solution
	private int dimension;

	//neighborhood size
	private int neighborhoodSize;

	//best neighborhood solution per particle
	private int[][] best;
	
	/**
	 * Constructor.
	 * @param particlesCount
	 * @param dimension
	 * @param neighborhoodSize
	 */
	public LocalNeighborhood(
			int particlesCount, int dimension, int neighborhoodSize) {
		
		this.dimension = dimension;
		this.neighborhoodSize = neighborhoodSize;
		
		best = new int[particlesCount][dimension];
	}



	/**
	 * Method finds the best solution in the swarm.
	 */
	@Override
	public void scan(Particle[] particle) {
		
		for (int index = 0; index < particle.length; index++) {
			
			int startFrom = index - neighborhoodSize/2;
			int endAt = index + neighborhoodSize/2;
			if (startFrom < 0) startFrom = 0;
			if (endAt >= particle.length) endAt = particle.length - 1;
			
			int bestIndex = startFrom;
			int bestValue = particle[bestIndex].getBestProjectDuration();
			
			for (int i = startFrom + 1; i<= endAt; i++) {
				
				if (particle[i].getBestProjectDuration() < bestValue) {
					
					bestValue = particle[i].getBestProjectDuration();
					bestIndex = i;
					
				}
				
			}
			
			for (int d = 0; d < dimension; d++) {
				best[index][d] = particle[bestIndex].getBestWorkUnitIndexes()[d];
			}
			
		}

	}

	/**
	 * Method returns the best solution for the particle
	 * if index forIndex.
	 */
	@Override
	public int[] findBest(int forIndex) {
		return best[forIndex];
	}

}