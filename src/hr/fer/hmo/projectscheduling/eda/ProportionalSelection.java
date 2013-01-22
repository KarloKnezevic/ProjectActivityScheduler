package hr.fer.hmo.projectscheduling.eda;

import java.util.Random;

/**
 * Proportional Selection.
 * Select Individuals with Roulette Wheel.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class ProportionalSelection implements ISelection {

	@Override
	public void select(EdaChromosome[] edaIndividuals,
			EdaChromosome[] selectedIndiviuals) {
		
		Random rand = new Random();
		
		int max = 0;
		int sum = 0;
		for (int i = 0; i  < edaIndividuals.length; i++) {
			sum += edaIndividuals[i].getActualDuration();
			if (i==0 || max < edaIndividuals[i].getActualDuration()) {
				max = edaIndividuals[i].getActualDuration();
			}
		}
		
		sum = edaIndividuals.length*max - sum;
		for (int i = 0; i < selectedIndiviuals.length; i++) {
			double random = rand.nextDouble()*sum;
			double accSum = 0;
			boolean choosen = false;
			for (int j = 0; j < edaIndividuals.length; j++) {
				accSum += max - edaIndividuals[j].getActualDuration();
				if (random < accSum) {
					selectedIndiviuals[i] = edaIndividuals[j];
					choosen = true;
					break;
				}
			}
			
			if (!choosen) {
				selectedIndiviuals[i] = edaIndividuals[edaIndividuals.length-1];
			}
		}
	}
}
