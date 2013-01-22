package hr.fer.hmo.projectscheduling.configuration;

import hr.fer.hmo.projectscheduling.common.Algorithm;
import hr.fer.hmo.projectscheduling.eda.EDA;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;

public class EDARun extends RunConfiguration {

	public EDARun(Map<String, Algorithm> algorithms) {
		super(algorithms);
	}
	
	public EDARun(Map<String, Algorithm> algorithms, 
			JProgressBar progress) {
		super(algorithms, progress);
	}

	@Override
	public String toString() {
		return "EDA";
	}

	@Override
	protected void run() {
		
		EDA eda = (EDA) algorithms.get("EDA");
		eda.setConfiguration(this);
		setMaxIterations(eda.getGenerations());
		eda.run();
		runBestIndividual = eda.getBestIndividual();
		
	}
	
	public static void main(String[] args) {
		
		Map<String, Algorithm> algorithms = new HashMap<String, Algorithm>();
		
		Algorithm eda = new EDA();
		algorithms.put(eda.toString(), eda);
		
		RunConfiguration edaRun = new EDARun(algorithms);
		edaRun.runConfiguration();

	}

}
