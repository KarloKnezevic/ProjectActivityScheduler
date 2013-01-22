package hr.fer.hmo.projectscheduling.configuration;

import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.eda.EDA;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;

public class CycleEDARun extends RunConfiguration {
	
	public CycleEDARun(Map<String, Algorithm> algorithms) {
		super(algorithms);
	}

	public CycleEDARun(Map<String, Algorithm> algorithms, JProgressBar progress) {
		super(algorithms, progress);
	}

	@Override
	public String toString() {
		return "Cycle(EDA -> EDA)";
	}

	@Override
	protected void run() {
		
		int cycles = 5;
		EDA eda = (EDA) algorithms.get("EDA");
		eda.setConfiguration(this);
		maxIterations += eda.getGenerations();
		for (int cycle = 0; cycle < cycles; cycle++) {
			maxIterations += eda.getGenerations();
			maxIterations += eda.getGenerations();
		}
		setMaxIterations(maxIterations);
		
		System.out.println(maxIterations);
		
		// Initial run
		eda.run();
		
		// One cycle = 2 EDA runs
		for (int cycle = 0; cycle < cycles; cycle++) {
			eda.setInitialIndividual(eda.getBestIndividual());
			eda.run();
			eda.setInitialIndividual(eda.getBestIndividual());
			eda.run();
		}
		
		runBestIndividual = eda.getBestIndividual();

	}

	public static void main(String[] args) {
		
		Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
		
		Algorithm eda = new EDA();
		algorithms.put(eda.toString(), eda);
		
		RunConfiguration cycleEDARun = new CycleEDARun(algorithms);
		cycleEDARun.runConfiguration();

	}

}
