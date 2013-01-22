package hr.fer.hmo.projectscheduling.pso;

/**
 * Neighborhood Interface.
 * Model of swarm neighborhood.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public interface Neighborhood {
	
	/**
	 * Abstraction of Neighborhood.
	 * @param particle
	 */
	public void scan (Particle[] particle);
	
	/**
	 * For particle with index param returns best particle
	 * in its neighborhood.
	 * @param forIndex
	 * @return particle indexes
	 */
	public int[] findBest(int forIndex);

}
