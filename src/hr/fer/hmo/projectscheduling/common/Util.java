package hr.fer.hmo.projectscheduling.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Util
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 */
public class Util {

	/**
	 * Reading a file.
	 * @param fileName
	 * @return individual
	 * @throws IOException
	 */
	public static List<ArrayList<WorkUnit>> readInputFile(String fileName) 
			throws IOException {
		
		InputStream inputStream;
		if (fileName.equals("/HOM-project.txt"))
			inputStream = Util.class.getResourceAsStream(fileName);
		else
			inputStream = new FileInputStream(fileName);
		
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						new BufferedInputStream(
								inputStream), "UTF-8"));

		String line;
		String[] splitedLine;
		List<ArrayList<WorkUnit>> projectLists = 
				new ArrayList<ArrayList<WorkUnit>>();

		while ((line = reader.readLine()) != null) {
			
			ArrayList<WorkUnit> project = new ArrayList<WorkUnit>();
			
			//1st place is project id; always ascending order 0 to n
			splitedLine = line.split("(\\s)*(:)*\\s+");
			int projectId = Integer.parseInt(splitedLine[0]);
			for (int i = 1; i < splitedLine.length; i += 2) {
				int workerId = Integer.parseInt(splitedLine[i]);
				int workLoad = Integer.parseInt(splitedLine[i+1]);
				project.add(new WorkUnit(projectId, workerId, workLoad));
			}
			
			projectLists.add(project);
			
		}
		
		reader.close();

		return projectLists;
		
	}
	
}