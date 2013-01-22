package hr.fer.hmo.projectscheduling.configuration;

import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.ga.GA;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;

public class GARun extends RunConfiguration {

	public GARun(Map<String, Algorithm> algorithms, JProgressBar progress) {
		super(algorithms, progress);
	}

	public GARun(Map<String, Algorithm> algorithms) {
		super(algorithms);
	}

	@Override
	public String toString() {
		return "GA";
	}

	@Override
	protected void run() {

		GA ga = (GA) algorithms.get("GA");
		ga.setConfiguration(this);
		setMaxIterations(ga.getIterations());
		ga.run();
		runBestIndividual = ga.getBestIndividual();

	}
	
	public static void main(String[] args) {
		
		Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
		
		Algorithm ga = new GA();
		algorithms.put(ga.toString(), ga);
		
		RunConfiguration gaRun = new GARun(algorithms);
		gaRun.runConfiguration();

	}

}
