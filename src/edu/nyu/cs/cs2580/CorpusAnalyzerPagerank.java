package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.nyu.cs.cs2580.SearchEngine.Options;



/**
 * @CS2580: Implement this class for HW3.
 */
public class CorpusAnalyzerPagerank extends CorpusAnalyzer {

	// To write partial graphs to file
	final int BULK_DOC_PROCESSING_SIZE = 1000;

	// Map to maintain documents in memory
	private Map<String, Integer> _docList = new HashMap<String, Integer>();

	// To store the adjacency List
	private List<ArrayList<Integer>> _graph = new ArrayList<ArrayList<Integer>>();
	//private DocProcessor dp = new DocProcessor(_options._corpusPrefix); 


	public CorpusAnalyzerPagerank(Options options) {
		super(options);
	}

	/**
	 * This function processes the corpus as specified inside {@link _options}
	 * and extracts the "internal" graph structure from the pages inside the
	 * corpus. Internal means we only store links between two pages that are both
	 * inside the corpus.
	 * 
	 * Note that you will not be implementing a real crawler. Instead, the corpus
	 * you are processing can be simply read from the disk. All you need to do is
	 * reading the files one by one, parsing them, extracting the links for them,
	 * and computing the graph composed of all and only links that connect two
	 * pages that are both in the corpus.
	 * 
	 * Note that you will need to design the data structure for storing the
	 * resulting graph, which will be used by the {@link compute} function. Since
	 * the graph may be large, it may be necessary to store partial graphs to
	 * disk before producing the final graph.
	 * @throws FileNotFoundException 
	 *
	 * @throws IOException
	 */

	// This will create docList in memory during mining mode
	// check if index is the right 
	private void createDocumentList() throws FileNotFoundException{
		int docId = 0; 
		File folder = new File(_options._corpusPrefix);
		for (final File fileEntry : folder.listFiles()){
			_docList.put(fileEntry.getName(), docId++);
		}
	}

	@Override
	public void prepare() throws IOException {
		System.out.println("Preparing " + this.getClass().getName());
		long startTime = System.nanoTime();
		createDocumentList();
		
		// Delete all file before creating the _graph
		File file =  new File(_options._indexPrefix + "/corpusGraph.txt");
		file.delete();
		//System.out.println(_docList.size());
		File folder = new File(_options._corpusPrefix);
		for (final File fileEntry : folder.listFiles()) {
			HeuristicLinkExtractor extractor = new HeuristicLinkExtractor(fileEntry);
			//System.out.println(extractor.getLinkSource());
			ArrayList<Integer> adjacencyList = new ArrayList<Integer>();
			String LinkSource = extractor.getLinkSource();
			adjacencyList.add(_docList.get(LinkSource));
			while(extractor.getNextInCorpusLinkTarget() != null){
				String linkTarget = extractor.getNextInCorpusLinkTarget();
				if(_docList.get(linkTarget) != null){
					adjacencyList.add(_docList.get(linkTarget));
				}}

			_graph.add(adjacencyList);
			if(_graph.size() == BULK_DOC_PROCESSING_SIZE ){
				writeFile(_graph);
				_graph.clear();
			}
		}
		
		if(!_graph.isEmpty()){
			writeFile(_graph);
			_graph.clear();
		}
		long endTime = System.nanoTime();

		System.out.println("Took prepare() "+(endTime - startTime)/1000000000.0 + " s");

	}

	// To write the partial graph to the file
	private void writeFile(List<ArrayList<Integer>> _grah) throws IOException{
		String path = _options._indexPrefix + "/corpusGraph.txt";
		File file = new File(path);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		for(List adjacencyList : _graph){
			// String util to convert a iterable to String with delimiter
			String tempString = StringUtils.join(adjacencyList, " ");
			writer.write(tempString + "\n");	
		}
		
		writer.close();
	}
	

	/**
	 * This function computes the PageRank based on the internal graph generated
	 * by the {@link prepare} function, and stores the PageRank to be used for
	 * ranking.
	 * 
	 * Note that you will have to store the computed PageRank with each document
	 * the same way you do the indexing for HW2. I.e., the PageRank information
	 * becomes part of the index and can be used for ranking in serve mode. Thus,
	 * you should store the whatever is needed inside the same directory as
	 * specified by _indexPrefix inside {@link _options}.
	 *
	 * @throws IOException
	 */
	@Override
	public void compute() throws IOException {
		System.out.println("Computing using " + this.getClass().getName());
		long startTime = System.nanoTime();
		String file = _options._indexPrefix + "/corpusGraph.txt";

		double docNum = _docList.size();
		int itrNum = 1; // iteration number, try itrNum = 1 and 2
		double lambda = 0.1; //try lambda = 0.1 and 0.9
		double[] prev = new double[(int) docNum];
		double[] next = new double[(int) docNum];
		System.out.println(1/docNum);
		//initialize prev to 1/docNum
		Arrays.fill(prev, 1/docNum);

		for(int itr = 0; itr < itrNum; itr++){
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
		
			// next = G*prev
			while((line = br.readLine()) != null){
				String[] strArr = line.split(" ");
				int docId = Integer.parseInt(strArr[0]);
				double adjNum = strArr.length - 1;
				for(int i = 1; i < strArr.length; i++){
					int adjId = Integer.parseInt(strArr[i]);
					next[adjId] += prev[docId]*(1.0/adjNum);
				}
			}
			br.close();
		
			// next = lambda*next + (1-lambda)*prev
			for(int i = 0; i < next.length; i++){
				next[i] = lambda*next[i] + (1-lambda)*prev[i];
			}
		
			prev = next;
			next = new double[(int) docNum];
		}
		
		File rfile = new File(_options._indexPrefix + "/pageRank.txt");
		rfile.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(rfile, true));

		// Write document number at the first line
		bw.write(_docList.size() + "\n");

		//write prev to file, it is page rank 
		for(int i = 0; i < prev.length; i++){
			bw.write(i + " " + Double.toString(prev[i])+"\n");
		}
		bw.close();

		long endTime = System.nanoTime();
		System.out.println("Took compute() "+(endTime - startTime)/1000000000.0 + " s");
		return;
	}

	/**
	 * During indexing mode, this function loads the PageRank values computed
	 * during mining mode to be used by the indexer.
	 *
	 * @throws IOException
	 */
	@Override
	public Object load() throws IOException {
		System.out.println("Loading using " + this.getClass().getName());
		String path = _options._indexPrefix + "/pageRank.txt";
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line = br.readLine();
		int docNum = Integer.parseInt(line);

		double[] pageRankLs = new double[docNum];
		
		// read score, line: <docId> <score>
		while((line = br.readLine()) != null){
			String[] arr = line.split(" ");
			int docId = Integer.parseInt(arr[0]);
			pageRankLs[docId] = Double.parseDouble(arr[1]);
		}
		br.close();
		return pageRankLs;
	}
}
