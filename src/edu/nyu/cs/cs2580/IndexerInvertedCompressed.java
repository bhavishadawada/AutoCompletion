package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW2.
 */
public class IndexerInvertedCompressed extends Indexer2 {
	/*
	private static final long serialVersionUID = -1785477383728439657L;

	// Data structure to maintain unique terms with id
	private Map<String, Integer> _dictionary = new HashMap<String, Integer>();

	// Data structure to store number of times a term occurs in Document
	// term id --> frequency
	private ArrayList<Integer> _documentTermFrequency = new ArrayList<Integer>();

	// Data structure to store number of times a term occurs in the complete Corpus
	// term id --> frequency
	private ArrayList<Integer> _corpusTermFrequency = new ArrayList<Integer>();
	
	private ArrayList<Integer> _termLineNum = new ArrayList<Integer>();
	

	// Data structure to store unique terms in the document
	//private Vector<String> _terms = new Vector<String>();

	// Stores all Document in memory.
	private List<DocumentIndexed> _documents = new ArrayList<DocumentIndexed>();
	*/
	private IndexerInvertedOccurrence _occurIndex;
	private ArrayList<PostListCompressed> _postListCompressed; 
	
	// use buffer of post list to reduce file IO
	private HashMap<String, PostListOccurence> _postListBuf = 
			new HashMap<String, PostListOccurence>();
	int _postListBufSize = 1000;
	
  public IndexerInvertedCompressed() { }
  public IndexerInvertedCompressed(Options options) {
    super(options);
    System.out.println("Using Indexer: " + this.getClass().getSimpleName());
	_occurIndex = new IndexerInvertedOccurrence(options);
  }

  @Override
  public void constructIndex() throws IOException {
	  _occurIndex.constructIndex();
	  this._dictionary = _occurIndex._dictionary;
	  this._documentTermFrequency = _occurIndex._documentTermFrequency;
	  this._corpusTermFrequency = _occurIndex._corpusTermFrequency;
	  this._documents = _occurIndex._documents;
	  this._postListCompressed = new ArrayList<PostListCompressed>(this._dictionary.size());
	  this._termLs2 = _occurIndex._termLs;

	  for(int i = 0; i < this._dictionary.size(); i++){
		  this._postListCompressed.add(new PostListCompressed());
	  }

	  // compress
	  List<String> files = Utility.getFilesInDirectory(_options._indexPrefix);
	  for (String file : files) {
		  if (file.endsWith(".idx") && !file.equals("corpus.idx")){
			  String fileName = _options._indexPrefix + "/" + file;
			  System.out.println("compress " + file);
			  BufferedReader br;
			  try {
				  br = new BufferedReader(new FileReader(fileName));
				  String line = br.readLine();
				  while(line != null){
					  PostListOccurence postList = _occurIndex.buildPostLs(line);
					  if(_dictionary.containsKey(postList.term)){
						  int termId = _dictionary.get(postList.term);
						  _postListCompressed.set(termId,  new PostListCompressed(postList));
					  }
					  line = br.readLine();
				  }
				  br.close();
			  } catch (FileNotFoundException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  } catch (IOException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }
		  }
	  }
	  
		System.out.println("_dictionary size: " + _dictionary.size());
		String indexFile = _options._indexPrefix + "/corpus.idx";

		File file = new File(indexFile);
		file.delete();

		System.out.println("Write Indexer to " + indexFile);

	    ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(indexFile));
	    writer.writeObject(this);
	    writer.close();
  }
  
	
  @Override
  public void loadIndex() throws IOException, ClassNotFoundException {
	    String indexFile = _options._indexPrefix + "/corpus.idx";
	    System.out.println("Load index from: " + indexFile);

	    ObjectInputStream reader = new ObjectInputStream(new FileInputStream(indexFile));

	    IndexerInvertedCompressed loaded = (IndexerInvertedCompressed) reader.readObject();

	    this._documents = loaded._documents;
	    this._dictionary = loaded._dictionary;
	    this._numDocs = this._documents.size();
	    this._corpusTermFrequency = loaded._corpusTermFrequency;
	    this._documentTermFrequency = loaded._documentTermFrequency;
	    this._termLs = loaded._termLs2;
	    for (Integer freq : loaded._corpusTermFrequency) {
	        this._totalTermFrequency += freq;
	    }
	    
	    this._postListCompressed = loaded._postListCompressed;
	    
	    System.out.println(Integer.toString(_numDocs) + " documents loaded " +
	        "with " + Long.toString(_totalTermFrequency) + " terms!");
	    reader.close();
	    
	    System.out.println("dic size: " + _dictionary.size());
	    
	    /*
	    String term = "advises";
	    int termId = _dictionary.get(term);
	    System.out.println(_postListCompressed.get(termId).deCompress());
	    */
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
	public PostListOccurence getPostList(String term){
		if(_postListBuf.size() > _postListBufSize){
			_postListBuf.clear();
		}
		if(_postListBuf.containsKey(term)){
			return _postListBuf.get(term);
		}
		if(_dictionary.containsKey(term)){
			int termId = _dictionary.get(term);
			PostListOccurence postList = _postListCompressed.get(termId).deCompress();
			_postListBuf.put(term, postList);
			return postList;
		}
		else{
			return null;
		}
	}

  /**
   * In HW2, you should be using {@link DocumentIndexed}
   */
	//TODO: This is to be implemented as discussed in class?????
	@Override
	public DocumentIndexed nextDoc(QueryPhrase query, int docid) {
		ArrayList<ArrayList<Integer>> postLsArr = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> cache = new ArrayList<Integer>();

		//query.processQuery();

		List<String> queryVector = query._tokens;
		for (String search : queryVector) {
			// build post list
 	
			PostListOccurence temp = getPostList(search);
			if(temp == null){
				return null;
			}
			else{
				ArrayList<Integer>  postLs =  new ArrayList<Integer>(temp.data.navigableKeySet());
				postLsArr.add(postLs);
				cache.add(0);
			}
		}
		
		if(postLsArr.size() > 0){
			Boolean hasNextDocId = true;
		    while(hasNextDocId){
		    	int nextDocId = -1;
		    	int cnt = 0;
		    	for(int i = 0; i < postLsArr.size(); i++){
		    		int c = cache.get(i);
		    		ArrayList<Integer> postLs = postLsArr.get(i);
		    		c = postLsNext(postLs, c, docid);
		    		cache.set(i, c);
		    		if(c == -1){
		    			hasNextDocId = false;
		    			break;
		    		}
		    		else{
		    			int currDocId = postLs.get(c);
		    			if(nextDocId == -1){
		    				nextDocId = currDocId;
		    				cnt++;
		    			}
		    			else{
		    				if(nextDocId == currDocId){
		    					cnt++;
		    				}
		    				else{
		    					nextDocId = Math.max(nextDocId, currDocId);
		    				}
		    			}
		    		}
		    	}
		    	if(cnt == postLsArr.size()){
		    		//System.out.println("document found " + nextDocId);
		    		//check phrase here
		    		boolean ret = true;
		    		for(List<String> phrase : query._phraseTokens){
		    			ret = ret & checkPhrase(phrase, nextDocId);
		    		}
		    		if(ret){
		    			return _documents.get(nextDocId);
		    		}
		    		else{
		    			docid = nextDocId;
		    		}
		    	}
		    	else{
		    		docid = nextDocId - 1;
		    	}
		    }
		    return null;
		}
		else{
			return null;
		}
	}
	
	public boolean checkPhrase(List<String> phrase, int docid){
		ArrayList<ArrayList<Integer>> occurLsArr = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> cache = new ArrayList<Integer>();
		for(String term : phrase){
			occurLsArr.add(getPostList(term).data.get(docid));
			cache.add(0);
		}
		Boolean hasNextDocId = true;
		int pos = -1;
		while(hasNextDocId){
			int cnt = 0;
			for(int i = 0; i < occurLsArr.size(); i++){
				int c = cache.get(i);
				ArrayList<Integer> occurLs = occurLsArr.get(i);
				c = occurNext(occurLs, c, pos);
				cache.set(i, c);
				if(c == -1){
					hasNextDocId = false;
					break;
				}
				else{
					if(cnt == 0){
						pos = occurLs.get(c);
						cnt++;
					}
					else{
						if(pos+1 == occurLs.get(c)){
							cnt++;
						}
						pos = Math.max(pos, occurLs.get(c));
						//System.out.println("pos: " + pos + "c: " + c);
					}
				}
			}
			if(cnt == phrase.size()){
				return true;
			}
		}
		return false;
	}
	
	public int occurNext(ArrayList<Integer> occurLs, int cache, int pos){
		int last = occurLs.size() - 1;
		if(cache<0){
			return -1;
		}
		else if(occurLs.get(last) <= pos){
			return -1;
		}

		while(cache < occurLs.size() && occurLs.get(cache) <= pos){
			cache++;
		}

		if(cache == occurLs.size()){
			return -1;
		}
		else{
			return cache;
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

  /**
   * @CS2580: Implement this for bonus points.
   */
  @Override
  public int documentTermFrequency(String term, int docid) {
	if(_dictionary.containsKey(term) && docid < _documents.size()){
		PostListOccurence postLs = getPostList(term);
		return postLs.data.get(docid).size();
	}
	else{
		return 0;
	}
  }

  @Override
	public int documentTotalTermFrequency(int docid) {
		if(docid < _documents.size()){
			return _documents.get(docid)._termNum;
		}
		else{
			return 0;
		}
	}
@Override
protected void buildMapFromTokens(List<String> uniqueTermSet, int docId) {
	// TODO Auto-generated method stub
	
}
@Override
protected void writeFileHelper(Boolean record) throws IOException {
	// TODO Auto-generated method stub
	
}
@Override
protected void clearCharMap() {
	// TODO Auto-generated method stub
	
}
@Override
protected boolean isEmptyCharMap() {
	// TODO Auto-generated method stub
	return false;
}
@Override
protected void mergeAll() throws IOException {
	// TODO Auto-generated method stub
	
}
}
