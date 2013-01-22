package hr.fer.hmo.projectscheduling.eda;

/**
 * Interface ISelection.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public interface ISelection {
	
	/**
	 * Select Individuals from Eda Individuals and Puts them in Selected Individuals.
	 * @param edaIndividuals
	 * @param selectedIndiviuals
	 */
	public void select(EdaChromosome[] edaIndividuals, EdaChromosome[] selectedIndiviuals);

}
