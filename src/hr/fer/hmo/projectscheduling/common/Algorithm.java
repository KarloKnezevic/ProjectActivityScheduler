package hr.fer.hmo.projectscheduling.common;

import hr.fer.hmo.projectscheduling.configuration.RunConfiguration;

/**
 * Algorithm
 * @author Ivo Majić, ivo.majic2@fer.hr
 * @version 1.0
 */
public abstract class Algorithm {

	protected Individual bestIndividual = null;
	protected Individual initialIndividual = null;
	protected RunConfiguration configuration = null;

	public Individual getBestIndividual() {
		return bestIndividual;
	}

	public abstract void run();

	public void setInitialIndividual(Individual initialIndividual) {};

	@Override
	public abstract String toString();

	public void setConfiguration(RunConfiguration configuration) {
		this.configuration = configuration;
	}

}
