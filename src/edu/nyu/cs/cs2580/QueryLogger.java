
package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class QueryLogger {

	private static Map<Integer , WordTree>_queryLog = new HashMap<Integer, WordTree>();
	
	public static Map<Integer , WordTree> getLogger(){
		return _queryLog;
	} 
	
	// To add the query to the WordTree of a specific user
	public static void addQuery(int userId , String query){
		WordTree queryTree = _queryLog.get(userId);
		if(queryTree == null)
		{
			queryTree = new WordTree();
		}
		queryTree.add(query);
		
	}
	
	// Get the most frequently types queries by a user in a session
	public static List<String> getTopQueries(int userId, String partialQuery){
		WordTree queryTree = _queryLog.get(userId);
		return queryTree.suggest(partialQuery);
	}
}
