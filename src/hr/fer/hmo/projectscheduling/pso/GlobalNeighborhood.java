package hr.fer.hmo.projectscheduling.pso;

/**
 * Global Neighborhood.
 * All particles are aware of the best solutios in the swarm.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class GlobalNeighborhood implements Neighborhood {
	
	//best solution
	private int[] best;
	//dimension of solution
	private int dimension;
	
	/**
	 * Constructor.
	 * @param dimension
	 */
	public GlobalNeighborhood(int dimension) {

		this.dimension = dimension;
		best = new int[dimension];
	}

	/**
	 * Method finds the best solution in the swarm.
	 */
	@Override
	public void scan(Particle[] particle) {
		
		int bestIndex = 0;
		double bestValue = particle[bestIndex].getBestProjectDuration();
		
		for (int i = 1; i < particle.length; i++) {
			if (particle[i].getBestProjectDuration() < bestValue) {
				bestValue = particle[i].getBestProjectDuration();
				bestIndex = i;
			}
		}
		
		for (int i = 0; i < dimension; i++) {
			best[i] = particle[bestIndex].getBestWorkUnitIndexes()[i];
		}
		
	}

	/**
	 * Method returns the best solution for the particle
	 * if index forIndex.
	 */
	@Override
	public int[] findBest(int forIndex) {
		return best;
	}

}
