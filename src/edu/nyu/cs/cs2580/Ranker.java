package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * This is the abstract Ranker class for all concrete Ranker implementations.
 *
 * Use {@link Ranker.Factory} to create your concrete Ranker implementation. Do
 * NOT change the interface in this class!
 *
 * In HW1: {@link RankerFullScan} is the instructor's simple ranker and students
 * implement four additional concrete Rankers.
 *
 * In HW2: students will pick a favorite concrete Ranker other than
 * {@link RankerPhrase}, and re-implement it using the more efficient
 * concrete Indexers.
 *
 * 2013-02-16: The instructor's code went through substantial refactoring
 * between HW1 and HW2, students are expected to refactor code accordingly.
 * Refactoring is a common necessity in real world and part of the learning
 * experience.
 *
 * @author congyu
 * @author fdiaz
 */
public abstract class Ranker {
	// Options to configure each concrete Ranker.
	protected Options _options;
	// CGI arguments user provide through the URL.
	protected CgiArguments _arguments;

	// The Indexer via which documents are retrieved, see {@code IndexerFullScan}
	// for a concrete implementation. N.B. Be careful about thread safety here.
	protected Indexer _indexer;

	/**
	 * Constructor: the construction of the Ranker requires an Indexer.
	 */
	protected Ranker(Options options, CgiArguments arguments, Indexer indexer) {
		_options = options;
		_arguments = arguments;
		_indexer = indexer;
	}

	/**
	 * Processes one query.
	 * @param query the parsed user query
	 * @param numResults number of results to return
	 * @return Up to {@code numResults} scored documents in ranked order
	 */
	public Vector<ScoredDocument> runQuery(QueryPhrase query, int numResults){
		Queue<ScoredDocument> rankQueue = new PriorityQueue<ScoredDocument>();

		int docid = -1;
		Document doc = _indexer.nextDoc(query, docid);
		while(doc != null){
			rankQueue.add(runquery(query, doc));
			if(rankQueue.size() > numResults){
				rankQueue.poll();
			}
			docid = doc._docid;
			doc = _indexer.nextDoc(query, docid);
		}

		Vector<ScoredDocument> results = new Vector<ScoredDocument>();
		ScoredDocument scoredDoc = null;
		while ((scoredDoc = rankQueue.poll()) != null) {
			results.add(scoredDoc);
		}
		Collections.sort(results, Collections.reverseOrder());
		System.out.println("results size:" + results.size());
		return results;
	}

	public List<Map.Entry<String, Double>> psuedoRelevanceCalc(QueryPhrase query, int numResults , int numTerms){

		Vector<ScoredDocument> results = runQuery(query, numResults);
		// do the logic here

		Map<String, Double> frequencyMap = new HashMap<String,Double>();  
		for(ScoredDocument scoreddoc : results){
			Document doc = scoreddoc.getDocument();
			for(int i = 0 ; i < doc.termId.length; i++){
				int termId = doc.termId[i];
				String term = _indexer._termLs.get(termId);
				int termFrequency = doc.termFrequency[i];
				if(frequencyMap.containsKey(termId)){
					frequencyMap.put(term, frequencyMap.get(termId) + termFrequency);
				}
				else{
					frequencyMap.put(term, (double) termFrequency);
				}	  
			}  
		}

		// To get the Top m terms from k documents
		// Convert the hashMap to arrayList for Sorting
		List<Map.Entry<String, Double>> entries = new ArrayList<Map.Entry<String, Double>>(frequencyMap.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
			public int compare(
					Map.Entry<String, Double> entry1, Map.Entry<String, Double> entry2) {
				if(entry1.getValue() > entry2.getValue()){
					return -1;
				}
				else if(entry1.getValue() < entry2.getValue()){
					return 1;
				}
				else{
					return 0;
				}
			}
		});

		List<Map.Entry<String, Double>> top_M_TermsList =  entries.subList(0, numTerms);

		// normalize probability
		double sum = 0;
		for(Map.Entry<String,Double> entry : top_M_TermsList){
			sum = sum + entry.getValue();
		}

		for(Map.Entry<String,Double> entry : top_M_TermsList){
			entry.setValue(entry.getValue()/ sum);
		}

		return top_M_TermsList;	

	}

	public abstract ScoredDocument runquery(Query query, Document doc);

	/**
	 * All Rankers must be created through this factory class based on the
	 * provided {@code arguments}.
	 */
	public static class Factory {
		public static Ranker getRankerByArguments(CgiArguments arguments,
				Options options, Indexer indexer) {
			switch (arguments._rankerType) {
			case FULLSCAN:
				return new RankerFullScan(options, arguments, indexer);
			case CONJUNCTIVE:
				return new RankerConjunctive(options, arguments, indexer);
			case FAVORITE:
				return new RankerFavorite(options, arguments, indexer);
			case COMPREHENSIVE:
				return new RankerComprehensive(options, arguments, indexer);
			case COSINE:
				// Plug in your cosine Ranker
				break;
			case QL:
				// Plug in your QL Ranker
				break;
			case PHRASE:
				// Plug in your phrase Ranker
				break;
			case LINEAR:
				// Plug in your linear Ranker
				break;
			case NONE:
				// Fall through intended
			default:
				// Do nothing.
			}
			return null;
		}
	}
}
