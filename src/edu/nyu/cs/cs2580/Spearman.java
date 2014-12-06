package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

class Spearman{

	static public void main(String[] args) throws Exception{
		String pageRankPath = args[0];
		String numViewPath = args[1];

		Doc[] pageRank = genDoc(pageRankPath);
		Doc[] numView  = genDoc(numViewPath);
		System.out.println(cmpDoc(numView, pageRank));
	}

	static public Doc[] genDoc(String path) throws Exception{
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();

		int docNum = Integer.parseInt(line);
		
		// set docId
		Doc[] docLs = new Doc[docNum];
		for(int i = 0; i < docNum; i++){
			docLs[i] = new Doc();
			docLs[i].docId = i;
		}
		
		// read score, line: <docId> <score>
		while((line = br.readLine()) != null){
			String[] arr = line.split(" ");
			int docId = Integer.parseInt(arr[0]);
			docLs[docId].score = Double.parseDouble(arr[1]);
		}
		br.close();
		
		// sort docLs according to score
		ScoreComparator sc = new ScoreComparator();
		Arrays.sort(docLs, 0, docLs.length, sc);
		
		// assign rank according to the score sorting order
		for(int i = 0; i < docLs.length; i++){
			docLs[i].rank = i+1; // rank starts from 1
		}

		/*
		System.out.println("sort according to score");
		for(int i = 0; i < docLs.length; i++){
			System.out.println(docLs[i].docId + " " +docLs[i].score + " " + docLs[i].rank);
		}
		*/
		
		// sort docLs according to docId
		DocIdComparator dc = new DocIdComparator();
		Arrays.sort(docLs, 0, docLs.length, dc);

		/*
		System.out.println("sort according to docId");
		for(int i = 0; i < docLs.length; i++){
			System.out.println(docLs[i].docId + " " +docLs[i].score + " " + docLs[i].rank);
		}
		*/

		return docLs;
	}
	
	static public double cmpDoc(Doc[] doc0, Doc[] doc1) throws Exception{
		if(doc0.length != doc1.length){
			throw new Exception("cmpDoc Error: compare ranks with different length");
		}
		double tho = 0;
		double w = 0;
		double z = ((double)doc0.length + 1.0)/2;
		for(int i = 0; i < doc0.length; i++){
			tho += (doc0[i].rank - z)*(doc1[i].rank - z);
			w += Math.pow((doc0[i].rank - z), 2);
		}
		return tho/w;
	}
}
class Doc{
	int docId;
	double score;
	double rank;
}
class ScoreComparator implements Comparator<Doc>{
	@Override
	public int compare(Doc doc0, Doc doc1){
		if(doc0.score > doc1.score){
			return -1;
		}
		else if(doc0.score < doc1.score){
			return 1;
		}
		else{
			return 0;
		}
	}
}
class DocIdComparator implements Comparator<Doc>{
	@Override
	public int compare(Doc doc0, Doc doc1){
		if(doc0.docId> doc1.docId){
			return 1;
		}
		else if(doc0.docId< doc1.docId){
			return -1;
		}
		else{
			return 0;
		}
	}
}