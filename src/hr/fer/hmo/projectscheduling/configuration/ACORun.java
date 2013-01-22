package hr.fer.hmo.projectscheduling.configuration;

import hr.fer.hmo.projectscheduling.aco.ACO;
import hr.fer.hmo.projectscheduling.common.Algorithm;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;

public class ACORun extends RunConfiguration {

	public ACORun(Map<String, Algorithm> algorithms) {
		super(algorithms);
	}
	
	public ACORun(Map<String, Algorithm> algorithms, 
			JProgressBar progress) {
		super(algorithms, progress);
	}

	@Override
	public String toString() {
		return "ACO";
	}

	@Override
	protected void run() {
		
		ACO aco = (ACO) algorithms.get("ACO");
		aco.setConfiguration(this);
		setMaxIterations(aco.getWalkNumber());
		aco.run();
		runBestIndividual = aco.getBestIndividual();
		
	}
	
	public static void main(String[] args) {
		
		Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
		
		Algorithm aco = new ACO();
		algorithms.put(aco.toString(), aco);
		
		RunConfiguration acoRun = new ACORun(algorithms);
		acoRun.runConfiguration();

	}

}
