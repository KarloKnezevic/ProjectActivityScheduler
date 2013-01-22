package hr.fer.hmo.projectscheduling.configuration;

import hr.fer.hmo.projectscheduling.aco.ACO;
import hr.fer.hmo.projectscheduling.ais.ClonAlg;
import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.eda.EDA;
import hr.fer.hmo.projectscheduling.ga.GA;
import hr.fer.hmo.projectscheduling.pso.PSO;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;

public class CycleAlgorithmsRun extends RunConfiguration {

	public CycleAlgorithmsRun(Map<String, Algorithm> algorithms,
			JProgressBar progress) {
		super(algorithms, progress);
	}

	public CycleAlgorithmsRun(Map<String, Algorithm> algorithms) {
		super(algorithms);
	}

	@Override
	public String toString() {
		return "ACO -> ClonAlg -> Cycle(Remaining)";
	}

	@Override
	protected void run() {
		
		int cycles = 10;
		EDA eda = (EDA) algorithms.get("EDA");
		eda.setConfiguration(this);
		ClonAlg clonAlg = (ClonAlg) algorithms.get("ClonAlg");
		clonAlg.setConfiguration(this);
		PSO pso = (PSO) algorithms.get("PSO");
		pso.setConfiguration(this);
		ACO aco = (ACO) algorithms.get("ACO");
		aco.setConfiguration(this);
		GA ga = (GA) algorithms.get("GA");
		ga.setConfiguration(this);
		
		// Calculate max iterations
		maxIterations += aco.getWalkNumber();
		maxIterations += clonAlg.getIterations();
		for (int cycle = 0; cycle < cycles; cycle++) {
			
			maxIterations += eda.getGenerations();
			maxIterations += pso.getSwarmIterations();
			maxIterations += ga.getIterations();

		}
		setMaxIterations(maxIterations);
		
		aco.run();
		clonAlg.setInitialIndividual(aco.getBestIndividual());
		clonAlg.run();
		
		for (int cycle = 0; cycle < cycles; cycle++) {
			
			if (cycle == 0) {
				eda.setInitialIndividual(clonAlg.getBestIndividual());
				eda.run();
			} else {
				eda.setInitialIndividual(ga.getBestIndividual());
				eda.run();
			}
				
			pso.setInitialIndividual(eda.getBestIndividual());
			pso.run();

			ga.setInitialIndividual(pso.getBestIndividual());
			ga.run();
			
		}

		runBestIndividual = eda.getBestIndividual();

	}

	public static void main(String[] args) {
		
		Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
		
		Algorithm eda = new EDA();
		algorithms.put(eda.toString(), eda);
		Algorithm clonAlg = new ClonAlg();
		algorithms.put(clonAlg.toString(), clonAlg);
		Algorithm pso = new PSO();
		algorithms.put(pso.toString(), pso);
		Algorithm aco = new ACO();
		algorithms.put(aco.toString(), aco);
		Algorithm ga = new GA();
		algorithms.put(ga.toString(), ga);
		
		RunConfiguration cycleAlgorithmsRun = 
				new CycleAlgorithmsRun(algorithms);
		cycleAlgorithmsRun.runConfiguration();

	}

}
