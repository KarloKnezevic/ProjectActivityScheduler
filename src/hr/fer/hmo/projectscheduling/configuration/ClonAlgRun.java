package hr.fer.hmo.projectscheduling.configuration;

import hr.fer.hmo.projectscheduling.ais.ClonAlg;
import hr.fer.hmo.projectscheduling.common.Algorithm;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;

public class ClonAlgRun extends RunConfiguration {
	
	public ClonAlgRun(Map<String, Algorithm> algorithms) {
		super(algorithms);
	}

	public ClonAlgRun(Map<String, Algorithm> algorithms, 
			JProgressBar progress) {
		super(algorithms, progress);
	}

	@Override
	public String toString() {
		return "ClonAlg";
	}

	@Override
	protected void run() {
		
		// Algorithm initialization
		ClonAlg clonAlg = (ClonAlg) algorithms.get("ClonAlg");
		clonAlg.setConfiguration(this);
		
		// Maximum run iterations
		setMaxIterations(clonAlg.getIterations());
		
		clonAlg.run();
		runBestIndividual = clonAlg.getBestIndividual();

	}

	public static void main(String[] args) {
		
		Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
		
		Algorithm clonAlg = new ClonAlg();
		algorithms.put(clonAlg.toString(), clonAlg);
		
		RunConfiguration clonAlgRun = new ClonAlgRun(algorithms);
		clonAlgRun.runConfiguration();

	}

}
