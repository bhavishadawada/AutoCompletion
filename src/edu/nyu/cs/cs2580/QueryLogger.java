
package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

public class QueryLogger {

	private static Map<Integer, LinkedList<String>> _queryLog = 
								new HashMap<Integer, LinkedList<String>>();
	
	private static int resultSetSize = 2 ; 
	
	// To add the query to the beginning of LinkedList of a specific user
	public static void addQuery(int userId , String query){
		LinkedList<String> userList = _queryLog.get(userId);
		//double wordFrequency = -1 ; 
		if(userList.contains(query)){
			userList.remove(query);
		}
		_queryLog.get(userId).addFirst(query.toLowerCase());
		
	}
	
	// Get the most frequently types queries by a user in a session
	public static List<String> getTopQueries(int userId, String partialQuery){
		List<String> recentQueryList = new ArrayList<>();
		for(String str : _queryLog.get(userId)){
			if(str.toLowerCase().startsWith(partialQuery.toLowerCase())){
				recentQueryList.add(str);
			}
			if(recentQueryList.size() == resultSetSize){
				break;
			}
			
		}
		return recentQueryList;
	}
	
	public static boolean containsID(int id){
		return _queryLog.containsKey(id);
	}
	
	public static void addIdToMap(int id){
		_queryLog.put(id,  new LinkedList<String>());
	}
	
	public static int getId(){
		int id = _queryLog.size() + 1;
		addIdToMap(id);
		return id;
	}
	
	public static void main(String[] args) {
		QueryLogger.addIdToMap(1);
		QueryLogger.addQuery(1, "New York");
		QueryLogger.addQuery(1, "New York City");
		QueryLogger.addQuery(1, "world class");
		QueryLogger.addQuery(1, "new york university");
		System.out.println("New".startsWith("New"));
		List<String> result = QueryLogger.getTopQueries(1, "New");
		System.out.println(result.size());
		
		
	}
	
}


