package hr.fer.hmo.projectscheduling.configuration;

import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.pso.PSO;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;

public class PSORun extends RunConfiguration {

	public PSORun(Map<String, Algorithm> algorithms) {
		super(algorithms);
	}

	public PSORun(Map<String, Algorithm> algorithms, 
			JProgressBar progress) {
		super(algorithms, progress);
	}

	@Override
	public String toString() {
		return "PSO";
	}

	@Override
	protected void run() {

		PSO pso = (PSO) algorithms.get("PSO");
		pso.setConfiguration(this);
		setMaxIterations(pso.getSwarmIterations());
		pso.run();
		runBestIndividual = pso.getBestIndividual();

	}
	
	public static void main(String[] args) {
		
		Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
		
		Algorithm pso = new PSO();
		algorithms.put(pso.toString(), pso);
		
		RunConfiguration psoRun = new PSORun(algorithms);
		psoRun.runConfiguration();

	}

}
