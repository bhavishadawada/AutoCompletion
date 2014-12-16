
package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

public class QueryLogger {

	private static Map<Integer, LinkedList<Suggest>> _queryLog = 
								new HashMap<Integer, LinkedList<Suggest>>();
	
	private static int resultSetSize = 10 ; 
	
	// To add the query to the beginning of LinkedList of a specific user
	public static void addQuery(int userId , String query){
		query = query.toLowerCase();
		if(!_queryLog.containsKey(userId)){
			QueryLogger.addIdToMap(userId);
		}
		LinkedList<Suggest> userList = _queryLog.get(userId);
		double wordFrequency = -1 ; 
		for(Suggest sg : userList){
			if(sg.str.equals(query)){
				userList.remove(sg);
				wordFrequency = sg.score;
			}
		}
		_queryLog.get(userId).addFirst(new Suggest(wordFrequency +1 , query));
		
	}
	
	public static double normalize(double in, double threshold){
	    double out = 1 - Math.pow(Math.E, -in/threshold);
	    return out;
	}
	
	// Get the most frequently types queries by a user in a session
	public static List<Suggest> getTopQueries(int userId, String partialQuery){
		List<Suggest> recentQueryList = new ArrayList<>();
		if(_queryLog.containsKey(userId)){
		    for(Suggest sg: _queryLog.get(userId)){
		    	if(sg.str.startsWith(partialQuery.toLowerCase())){
		    		recentQueryList.add(new Suggest(normalize(sg.score, 1), sg.str));
		    	}
		    }
		}
		return recentQueryList;
	}
	
	public static boolean containsID(int id){
		return _queryLog.containsKey(id);
	}
	
	public static void addIdToMap(int id){
		_queryLog.put(id,  new LinkedList<Suggest>());
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
		List<Suggest> result = QueryLogger.getTopQueries(1, "New");
		for(Suggest sg : result)
			System.out.println(sg);
	}
	
}


