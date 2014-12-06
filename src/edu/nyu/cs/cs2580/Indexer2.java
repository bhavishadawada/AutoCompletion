package edu.nyu.cs.cs2580;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.nyu.cs.cs2580.SearchEngine.Options;

public abstract class Indexer2 extends Indexer implements Serializable{
	  /**
	 * 
	 */
	private static final long serialVersionUID = 5380744807563119350L;
	final int BULK_DOC_PROCESSING_SIZE = 1000;

	// Data structure to maintain unique terms with id
	Map<String, Integer> _dictionary = new HashMap<String, Integer>();
	
	ArrayList<String> _termLs2;

	// Data structure to store number of times a term occurs in Document
	// term id --> frequency
	ArrayList<Integer> _documentTermFrequency = new ArrayList<Integer>();

	// Data structure to store number of times a term occurs in the complete Corpus
	// term id --> frequency
	ArrayList<Integer> _corpusTermFrequency = new ArrayList<Integer>();

	ArrayList<Integer> _termLineNum = new ArrayList<Integer>();

	// Data structure to store unique terms in the document
	//private Vector<String> _terms = new Vector<String>();

	// Stores all Document in memory.
	List<DocumentIndexed> _documents = new ArrayList<DocumentIndexed>();
	
	double[] pageRankLs;

	int[] numViewLs;

	public Indexer2() { }
	public Indexer2(Options options) {
	  super(options);
	}

	protected abstract void buildMapFromTokens(List<String> uniqueTermSet, int docId);

	protected abstract void writeFileHelper(Boolean record) throws IOException;
	
	protected abstract void clearCharMap();
	
	protected abstract boolean isEmptyCharMap();
	
	protected abstract void mergeAll()throws IOException;
	
	@Override
	public void constructIndex() throws IOException {
		String corpusFile = _options._corpusPrefix;
		System.out.println("Construct index from: " + corpusFile);
		long startTime = System.nanoTime();
		//delete everything in index before constructIndex

		CorpusAnalyzer analyzer = new CorpusAnalyzerPagerank(_options);
		pageRankLs = (double[]) analyzer.load();

		LogMiner miner = new LogMinerNumviews(_options);
		numViewLs = (int[]) miner.load();

		deleteFile();
		DocProcessor dp = new DocProcessor(_options._corpusPrefix);
		while (dp.hasNextDoc()) {
			// The problem is this will include num_views also
			dp.nextDoc();
			processDocument(dp.title, dp.body);

			if(_numDocs % BULK_DOC_PROCESSING_SIZE == 0){
				writeFileHelper(false);
				clearCharMap();
				//writeFrequency(_corpusTermFrequency);
				//_corpusTermFrequency.clear();
			}
		}

		// if the documents are  < BULK_DOC_PROCESSING_SIZE
		if (!isEmptyCharMap()) {
			writeFileHelper(false);
			clearCharMap();
		}
		mergeAll();

		System.out.println("_dictionary size: " + _dictionary.size());
		String indexFile = _options._indexPrefix + "/corpus.idx";
		System.out.println("Write Indexer to " + indexFile);

		System.out.println("termLs size: " + this._termLs.size());
		ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));
		this._termLs2 = this._termLs;
		writer.writeObject(this);
		writer.close();
		long endTime = System.nanoTime();
		System.out.println("Took ConstructIndex"+(endTime - startTime)/1000000000.0 + " s");
	}

	private void processDocument(String title, String body) throws IOException {
		int docId = _numDocs;
		++ _numDocs;
		Set<String> uniqueTermSetBody = Utility.tokenize(body);
		List<String> bodyTermVector = Utility.tokenize2(body);
		buildMapFromTokens(bodyTermVector,docId);

		DocumentIndexed doc = new DocumentIndexed(docId);
		
		Map<Integer,Integer> termFrequencyMap = new HashMap<Integer,Integer>();
		//build _dictionary
		for(String token:uniqueTermSetBody){
			if(!_dictionary.containsKey(token)){
				_dictionary.put(token, _corpusTermFrequency.size());
				_corpusTermFrequency.add(0);
				_documentTermFrequency.add(0);
				_termLineNum.add(0);
				_termLs.add(token);
			}
			int id = _dictionary.get(token);
			_documentTermFrequency.set(id, _documentTermFrequency.get(id) + 1);
		}

		doc.termId = new int[uniqueTermSetBody.size()];
		doc.termFrequency = new int[uniqueTermSetBody.size()];
		for(String token : bodyTermVector){
			int id = _dictionary.get(token);
			_corpusTermFrequency.set(id, _corpusTermFrequency.get(id) + 1);
			
			if(termFrequencyMap.containsKey(id)){
				termFrequencyMap.put(id, termFrequencyMap.get(id) + 1);
			}
			else{
				termFrequencyMap.put(id, 1);
			}
			
		}
		
		// Convert the hashMap to arrayList for Sorting
		List<Map.Entry<Integer, Integer>> entries = new ArrayList<Map.Entry<Integer, Integer>>(termFrequencyMap.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<Integer, Integer>>() {
			  public int compare(
			      Map.Entry<Integer, Integer> entry1, Map.Entry<Integer, Integer> entry2) {
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
		
		int indexNum = 0;
		for(Entry<Integer,Integer> entry : entries){
			doc.termId[indexNum] = entry.getKey().intValue();
			doc.termFrequency[indexNum] = entry.getValue().intValue();
			indexNum = indexNum + 1;
		}	

		doc._pageRank = (float)pageRankLs[docId];
		doc._numViews = numViewLs[docId];
		doc.setTitle(title);
		doc._termNum = bodyTermVector.size();
		doc.setUrl(Integer.toString(docId));
		_documents.add(doc);

	}

	@Override
	public void loadIndex() throws IOException, ClassNotFoundException {
		String indexFile = _options._indexPrefix + "/corpus.idx";
		System.out.println("Load index from: " + indexFile);

		ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));

		Indexer2 loaded = (Indexer2) reader.readObject();
		System.out.println("loaded termLs2 size: " + loaded._termLs2.size());

		this._documents = loaded._documents;
		this._dictionary = loaded._dictionary;
		this._termLs = loaded._termLs2;
		this._numDocs = _documents.size();
		this._corpusTermFrequency = loaded._corpusTermFrequency;
		this._documentTermFrequency = loaded._documentTermFrequency;
		this._termLineNum = loaded._termLineNum;
		for (Integer freq : loaded._corpusTermFrequency) {
			this._totalTermFrequency += freq;
		}
		System.out.println(Integer.toString(_numDocs) + " documents loaded " +
				"with " + Long.toString(_totalTermFrequency) + " terms!");
		reader.close();

		/*
		    Query query = new Query("Alfred Matthew");
		    Document doc = nextDoc(query, -1);
		    System.out.println(doc.getTitle());
		    doc = nextDoc(query, doc._docid);
		    System.out.println(doc.getTitle());
		 */
	}
	
	private void deleteFile(){
		String path = _options._indexPrefix + "/";
		File dir = new File(path);
		File[] fileLs = dir.listFiles();
		for(File file : fileLs){
			if(file.getName().endsWith(".idx")){
			file.delete();
			}
		}
	}
	
	// return a pos such that posLs.get(pos)> docid
	public int postLsNext(ArrayList<Integer> postLs, int cache, int docid){
		int last = postLs.size() - 1;
		if(cache < 0){
			return -1;
		}
		else if(cache > last){
			return -1;
		}
		else if(postLs.get(last) <= docid){
			return -1;
		}
		while(cache < postLs.size() && postLs.get(cache) <= docid){
			cache++;
		}

		if(postLs.get(cache) > docid){
			return cache;
		}
		else{
			return -1;
		}
	}
	
	// number of documents the term occurs in
	@Override
	public int corpusDocFrequencyByTerm(String term) {
		return _dictionary.containsKey(term) ?
				_documentTermFrequency.get(_dictionary.get(term)) : 0;
	}

	//number of times a term appears in corpus
	@Override
	public int corpusTermFrequency(String term) {
		return _dictionary.containsKey(term) ?
				_corpusTermFrequency.get(_dictionary.get(term)) : 0;
	}
	
	@Override
	public DocumentIndexed getDoc(int docid) {
		if(docid < _documents.size()){
			return _documents.get(docid);
		}
		else{
			return null;
		}
	}
}
