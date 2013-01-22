package hr.fer.hmo.projectscheduling.pso;

import hr.fer.hmo.projectscheduling.common.WorkUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * PSO Util.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class PSOUtil {
	
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

	//permutates subarray elements in array.
	public static void shuffleSubArray(int[] array, int subArrayLength, Random rand) {
		
		if (subArrayLength == 0) return;

		int subArrays = array.length / subArrayLength;

		for (int subArraysNum = 0; subArraysNum < subArrays; subArraysNum++) {

			int subArrayEndIndex = (subArraysNum+1)*subArrayLength;
			int subArrayStartIndex = subArrayEndIndex - subArrayLength;

			for (int i = subArrayEndIndex; i > subArrayStartIndex + 1; i--) {
				int b = rand.nextInt(subArrayEndIndex-subArrayStartIndex) + subArrayStartIndex;
				if (b != i-1) {
					int e = array[i-1];
					array[i-1] = array[b];
					array[b] = e;
				}
			}

		}

	}
	
	//reset map
	public static void reset() {
		
		indexes2WorkUnitsMap = null;
		
	}

}