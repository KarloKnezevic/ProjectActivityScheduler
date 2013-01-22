package hr.fer.hmo.projectscheduling.aco;

import hr.fer.hmo.projectscheduling.common.WorkUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ACO Util.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 2.0
 */
public class ACOUtil {

	//Map for Indexes and WorkUnits
	private static Map<Integer, WorkUnit> indexes2WorkUnitsMap;

	//Mapping Indexes and WorkUnits
	public static Map<Integer, WorkUnit> createIndexes2WorkUnitMap(List<ArrayList<WorkUnit>> projectWorkLists) {

		if (indexes2WorkUnitsMap != null) return indexes2WorkUnitsMap;

		indexes2WorkUnitsMap = new HashMap<Integer, WorkUnit>();
		int subProjectActivities = projectWorkLists.get(0).size();
		for (int i = 0; i < projectWorkLists.size(); i++) {

			for (int j = 0; j < subProjectActivities; j++) {

				indexes2WorkUnitsMap.put(
						i*subProjectActivities+j, projectWorkLists.get(i).get(j));

			}

		}

		return indexes2WorkUnitsMap;

	}

	//linear array fill; 0 to array.length
	public static void linearFillArray(int[] array) {

		for (int i = 0; i < array.length; i++) {
			array[i] = i;
		}

	}

	//start is value of first index
	public static void changeReachable(int[] array, int start) {

		for (int i = 0; i < array.length; i++) {
			array[i] = start+i;
		}

	}

	//permutates array elements
	public static void shuffleArray(int[] array, Random rand) {

		for (int i = array.length; i > 1; i--) {
			int b = rand.nextInt(i);
			if (b != i-1) {
				int e = array[i-1];
				array[i-1] = array[b];
				array[b] = e;
			}
		}

	}

	//partial sort; method sorts first number elements
	public static void partialSort(Ant[] population, int number) {

		for (int i = 0; i < number; i++) {

			int best = i;
			for (int j = i+1; j < population.length; j++) {
				//see compareTO() method specification
				if (population[best].compareTo(population[j]) == 1) {
					best = j;
				}
			}
			if (best != i) {
				Ant tmp = population[i];
				population[i] = population[best];
				population[best] = tmp;
			}

		}

	}

	//reset map
	public static void reset() {

		indexes2WorkUnitsMap = null;

	}
}