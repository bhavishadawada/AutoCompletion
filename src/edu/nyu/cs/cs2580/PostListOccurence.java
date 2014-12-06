package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map.Entry;

public class PostListOccurence {
	String term;
	TreeMap<Integer, ArrayList<Integer>> data;

	public PostListOccurence(String term, TreeMap<Integer, ArrayList<Integer>> postList){
		this.term = term;
		this.data = postList;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(term + "::");
		for(Entry<Integer, ArrayList<Integer>> entry : data.entrySet()) {
			Integer docId = entry.getKey();
			ArrayList<Integer> occurList = entry.getValue();
			sb.append(docId).append(":").append(occurList).append("  ");
		}
		return sb.toString();
	}
}
