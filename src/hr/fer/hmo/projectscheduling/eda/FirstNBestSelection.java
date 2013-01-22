package hr.fer.hmo.projectscheduling.eda;

/**
 * First N Best Selection.
 * Select First SelectedIndiviuals.size Individuals from Eda Individuals
 * to Selected Individuals.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class FirstNBestSelection implements ISelection {

	@Override
	public void select(EdaChromosome[] edaIndividuals,
			EdaChromosome[] selectedIndiviuals) {
		
		for (int i = 0; i < selectedIndiviuals.length; i++) {
			
			selectedIndiviuals[i] = edaIndividuals[i];
			
		}
		
	}

}
