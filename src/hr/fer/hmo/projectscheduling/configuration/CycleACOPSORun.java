package hr.fer.hmo.projectscheduling.configuration;

import hr.fer.hmo.projectscheduling.aco.ACO;
import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.pso.PSO;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;

public class CycleACOPSORun extends RunConfiguration {

	public CycleACOPSORun(Map<String, Algorithm> algorithms,
			JProgressBar progress) {
		super(algorithms, progress);
	}

	public CycleACOPSORun(Map<String, Algorithm> algorithms) {
		super(algorithms);
	}

	@Override
	public String toString() {
		return "ACO -> Cycle(PSO)";
	}

	@Override
	protected void run() {
		
		int cycles = 20;
		ACO aco = (ACO) algorithms.get("ACO");
		aco.setConfiguration(this);
		PSO pso = (PSO) algorithms.get("PSO");
		pso.setConfiguration(this);
		
		// Calculate max iterations
		maxIterations += aco.getWalkNumber();
		for (int cycle = 0; cycle < cycles; cycle++) {
			maxIterations += pso.getSwarmIterations();
		}
		setMaxIterations(maxIterations);
		
		aco.run();
		for (int cycle = 0; cycle < cycles; cycle++) {
			
			if (cycle == 0)
				pso.setInitialIndividual(aco.getBestIndividual());
			else
				pso.setInitialIndividual(pso.getBestIndividual());
			
			pso.run();
			
		}

		runBestIndividual = pso.getBestIndividual();

	}

	public static void main(String[] args) {
		
		Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
		
		Algorithm aco = new ACO();
		algorithms.put(aco.toString(), aco);
		Algorithm pso = new PSO();
		algorithms.put(pso.toString(), pso);
		
		RunConfiguration cycleACOPSORun = new CycleACOPSORun(algorithms);
		cycleACOPSORun.runConfiguration();

	}

}
