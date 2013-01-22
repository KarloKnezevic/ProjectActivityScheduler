package hr.fer.hmo.projectscheduling.configuration;

import hr.fer.hmo.projectscheduling.ais.ClonAlg;
import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.eda.EDA;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;

public class CycleEDAClonAlgRun extends RunConfiguration {

	public CycleEDAClonAlgRun(Map<String, Algorithm> algorithms,
			JProgressBar progress) {
		super(algorithms, progress);
	}

	public CycleEDAClonAlgRun(Map<String, Algorithm> algorithms) {
		super(algorithms);
	}

	@Override
	public String toString() {
		return "Cycle(EDA -> 10% ClonAlg)";
	}

	@Override
	protected void run() {
		
		int cycles = 20;
		EDA eda = (EDA) algorithms.get("EDA");
		eda.setConfiguration(this);
		ClonAlg clonAlg = (ClonAlg) algorithms.get("ClonAlg");
		clonAlg.setConfiguration(this);
		
		// Calculate max iterations
		for (int cycle = 0; cycle < cycles; cycle++) {
			
			maxIterations += eda.getGenerations();
			if ( cycle % 10 == 0) {
				maxIterations += clonAlg.getIterations();
				maxIterations += eda.getGenerations();
			}
		}
		setMaxIterations(maxIterations);
		
		for (int cycle = 0; cycle < cycles; cycle++) {

			System.out.println(((double) cycle/999.0 * 100) + "%");

			eda.setInitialIndividual(eda.getBestIndividual());
			eda.run();

			if ( cycle % 10 == 0) {
				
				clonAlg.setInitialIndividual(eda.getBestIndividual());
				clonAlg.run();

				eda.setInitialIndividual(clonAlg.getBestIndividual());
				eda.run();
				
			}
			
		}

		runBestIndividual = eda.getBestIndividual();

	}

	public static void main(String[] args) {
		
		Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
		
		Algorithm eda = new EDA();
		algorithms.put(eda.toString(), eda);
		Algorithm clonAlg = new ClonAlg();
		algorithms.put(clonAlg.toString(), clonAlg);
		
		RunConfiguration cycleEDAClonAlgRun = new CycleEDAClonAlgRun(algorithms);
		cycleEDAClonAlgRun.runConfiguration();

	}

}
