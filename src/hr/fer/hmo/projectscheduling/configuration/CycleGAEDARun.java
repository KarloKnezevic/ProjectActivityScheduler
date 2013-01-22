package hr.fer.hmo.projectscheduling.configuration;

import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.eda.EDA;
import hr.fer.hmo.projectscheduling.ga.GA;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;

public class CycleGAEDARun extends RunConfiguration {

	public CycleGAEDARun(Map<String, Algorithm> algorithms,
			JProgressBar progress) {
		super(algorithms, progress);
	}

	public CycleGAEDARun(Map<String, Algorithm> algorithms) {
		super(algorithms);
	}

	@Override
	public String toString() {
		return "Cycle(GA -> EDA)";
	}

	@Override
	protected void run() {
		
		int cycles = 10;
		GA ga = (GA) algorithms.get("GA");
		ga.setConfiguration(this);
		EDA eda = (EDA) algorithms.get("EDA");
		eda.setConfiguration(this);
		
		// Calculate max iterations
		maxIterations += ga.getIterations();
		for (int cycle = 0; cycle < cycles; cycle++) {
			maxIterations += eda.getGenerations();
			maxIterations += ga.getIterations();
		}
		setMaxIterations(maxIterations);
		
		ga.run();
		for (int cycle = 0; cycle < cycles; cycle++) {
				
			eda.setInitialIndividual(ga.getBestIndividual());
			eda.run();

			ga.setInitialIndividual(eda.getBestIndividual());
			ga.run();
			
		}

		runBestIndividual = ga.getBestIndividual();

	}

	public static void main(String[] args) {
		
		Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
		
		Algorithm ga = new GA();
		algorithms.put(ga.toString(), ga);
		Algorithm eda = new EDA();
		algorithms.put(eda.toString(), eda);
		
		RunConfiguration cycleGAEDARun = new CycleGAEDARun(algorithms);
		cycleGAEDARun.runConfiguration();

	}

}
