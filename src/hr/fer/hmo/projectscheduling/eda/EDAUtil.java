package hr.fer.hmo.projectscheduling.eda;

import hr.fer.hmo.projectscheduling.common.WorkUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * EDA Utility.
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class EDAUtil {

	//Map for Indexes and WorkUnits
	private static Map<Integer, WorkUnit> indexes2WorkUnitsMap;
	
	//list of hash maps: for every locus number of different genes
	private static List<Map<Integer, Integer>> statisticList;
	
	//how many selected individuals
	private static int selectedIndividuals;
	
	//parameter for smoothing
	private static double λ = 0;
	
	private static Random rand = new Random();

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
	public static void shuffleSubArray(int[] array, int subArrayLength) {

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
	
	/**
	 * Method calculates parameters of distribution and
	 * samples new individual.
	 * @return
	 */
	public static int[] multivariateUniqueDistribution() {
		
		//dimension
		//MAGIC NUMBER - HARD CODED!
		int dimension = 225;
		int subProjectActivities = 15;
		//--------------------------------------
		
		int cnt = 0;
		//tabu list of selected genes (individual has unique workers)
		List<Integer> tabu = new ArrayList<Integer>();
		//free indexes to use
		List<Integer> notUsedIndexes = 
				EDAUtil.linearFillList(0, subProjectActivities-1);
		Map<Integer, Integer> hm;
		//new individual
		int[] edaIndividual = new int[dimension];
		
		//MAIN LOOP
		for (int i = 0; i < edaIndividual.length; i++) {
			
			//reset tabu and freeIndexesList for every subList
			if (cnt == subProjectActivities) {
				cnt = 0;
				
				tabu.clear();
				notUsedIndexes = 
						EDAUtil.linearFillList(i, i+subProjectActivities-1);
			}
			
			//eject from statistic list forbidden genes
			hm = tabuRefresh(statisticList.get(i), tabu);
			//rotate the wheel
			int value = rouletteWheel(hm);
			
			//if random 
			if (value == -1) {
				//take random index from not used indexes
				value = randomPick(notUsedIndexes);
			}
			
			//SET VALUE
			edaIndividual[i] = value;
			
			//add this value to tabu and make it forbidden
			tabu.add(value);
			notUsedIndexes.remove((Object)value);
			
			cnt++;
			
		}
		
		//return sampled individual
		return edaIndividual;
		
	}
	
	/**
	 * Random pick index.
	 * @param notUsedIndexes
	 * @return
	 */
	private static int randomPick(List<Integer> notUsedIndexes) {
		//return random index
		return notUsedIndexes.get(rand.nextInt(notUsedIndexes.size()));

	}

	/**
	 * Roulette Wheel.
	 * @param hm
	 * @return
	 */
	private static int rouletteWheel(Map<Integer, Integer> hm) {
		
		//class numbers
		//+1 is "empty box"; random picking
		int C = hm.size() + 1;
		//IF λ=0, NO RANDOM PICKING; ONLY STATISTIC; COULD BE OVERTRAIN
		int sum = selectedIndividuals + (int)Math.ceil(C*λ);
		
		double accSum = 0;
		//make roulette
		double random = rand.nextDouble();
		
		for (Map.Entry<Integer, Integer> e : hm.entrySet()) {
			
			Integer key = e.getKey();
			Integer value = e.getValue();
			
			accSum += (value + λ) / sum;
			
			if (random < accSum) return key; 
			
		}

		//random picking
		return -1;
		
	}

	/**
	 * Tabu refresh.
	 * @param map
	 * @param tabu
	 * @return map/tabu
	 */
	private static Map<Integer, Integer> tabuRefresh(Map<Integer, Integer> map, List<Integer> tabu) {
		
		Map<Integer, Integer> mapCopy = new HashMap<Integer, Integer>();
		
		for (Map.Entry<Integer, Integer> e : map.entrySet()) {
			
			Integer key = e.getKey();
			Integer value = e.getValue();
			
			if (!tabu.contains(key.intValue())) {
				mapCopy.put(key, value);
			}
			
		}
		
		return mapCopy;
	}

	//linear fill list
	private static List<Integer> linearFillList(int start, int end) {
		
		List<Integer> integerList = new ArrayList<Integer>();
		
		for (int i = start; i <= end; i++) {
			
			integerList.add(i);
			
		}
		
		return integerList;
		
	}

	//reset map
	public static void reset() {

		indexes2WorkUnitsMap = null;

	}
	
	/**
	 * Setting statistic list.
	 * @param statisticList
	 */
	public static void setStatisticList(List<Map<Integer, Integer>> statisticList) {
		
		selectedIndividuals = 0;
		
		for (Map.Entry<Integer, Integer> e : statisticList.get(0).entrySet()) {
			selectedIndividuals += statisticList.get(0).get(e.getKey());
		}
		
		EDAUtil.statisticList = statisticList;
		
	}
	
	/**
	 * Setting Smoothing Parameter λ.
	 * @param L
	 */
	public static void setLambda(double L) {
		
		λ = L;
		
	}
	
	/**
	 * Setting Random for Sync.
	 * @param rand
	 */
	public static void setRand(Random rand) {
		
		EDAUtil.rand = rand;
		
	}

}