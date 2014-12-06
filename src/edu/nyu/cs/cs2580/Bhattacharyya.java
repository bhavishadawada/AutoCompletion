package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bhattacharyya {

	public static void main(String[] args) throws Exception{
		String path_to_prf_output = args[0];
		String path_to_output = args[1];
		

		File folder = new File(path_to_prf_output);
		File[] fileList = folder.listFiles();	
		List<QueryEntry> resultEntry = computeCoefficient(fileList);
		
		// To write to the output file
		File file = new File(path_to_output);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		for(QueryEntry entry : resultEntry){
			String tempString = entry.query_1 + " " + entry.query_2 + " " + entry.coefficient;
			writer.write(tempString + "\n");
		}
		
		writer.close();
	}

	public static List<QueryEntry> computeCoefficient(File[] fileList) throws IOException{
		List<QueryEntry> resultEntry = new ArrayList<QueryEntry>();
		for(int index = 0  ; index < fileList.length ; index++){
			Map<String, Double> map = new HashMap<String, Double>();
			BufferedReader br = new BufferedReader(new FileReader(fileList[index]));
			String line = null;
			while((line = br.readLine()) != null){
				String[] arr = line.split("\\t");
				double prob = Double.parseDouble(arr[1]);
				map.put(arr[0], prob);
			}
			List<QueryEntry> tempResult = computeCoefficientHelper(map , index , fileList);
			resultEntry.addAll(tempResult);
		}
		
		return resultEntry;
	}

	static List<QueryEntry> computeCoefficientHelper(Map<String, Double> map ,int index , File[] fileList) throws NumberFormatException, IOException{
		List<QueryEntry> resultEntry = new ArrayList<QueryEntry>();
		for(int i = index + 1; i < fileList.length ; i++){
			double coefficient = 0;
			QueryEntry entry = new QueryEntry();
			entry.query_1 = fileList[index].getName();
			entry.query_2 = fileList[i].getName();
			Map<String, Double> tempMap = new HashMap<String, Double>();
			BufferedReader br = new BufferedReader(new FileReader(fileList[i]));
			String line = null;
			while((line = br.readLine()) != null){
				String[] arr = line.split("\\t");
				double prob = Double.parseDouble(arr[1]);
				tempMap.put(arr[0], prob);
			}
			
			for(String term : map.keySet()){
				if(tempMap.containsKey(term)){
					double product = map.get(term) * tempMap.get(term);
					coefficient += Math.sqrt(product);
				}
			}
			
			entry.coefficient = coefficient;
			resultEntry.add(entry);			
		}
		
		return resultEntry;
	}
	
}

 class QueryEntry{
	String query_1;
	String query_2;
	double coefficient;
}
