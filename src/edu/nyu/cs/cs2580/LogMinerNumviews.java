package edu.nyu.cs.cs2580;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3.
 */
public class LogMinerNumviews extends LogMiner {

	// To write partial graphs to file
	final int BULK_DOC_PROCESSING_SIZE = 1000;

	// Map to maintain documents in memory
	private Map<String, Integer> _docList = new HashMap<String, Integer>();

	// Map to maintain numViews
	private Map<Integer, Integer> _numViews = new HashMap<Integer, Integer>();


	public LogMinerNumviews(Options options) {
		super(options);
	}

	/**
	 * This function processes the logs within the log directory as specified by
	 * the {@link _options}. The logs are obtained from Wikipedia dumps and have
	 * the following format per line: [language]<space>[article]<space>[#views].
	 * Those view information are to be extracted for documents in our corpus and
	 * stored somewhere to be used during indexing.
	 *
	 * Note that the log contains view information for all articles in Wikipedia
	 * and it is necessary to locate the information about articles within our
	 * corpus.
	 *
	 * @throws IOException
	 */

	// Think if this is already there in memory
	private void createDocumentList() throws FileNotFoundException{
		int docId = 0; 
		File folder = new File(_options._corpusPrefix);
		for (final File fileEntry : folder.listFiles()){
			_docList.put(fileEntry.getName(), docId++);
		}
	}

	// for every line in log, if the article is in corpus, store the views to a file
	@Override
	public void compute() throws IOException {
		System.out.println("Computing using " + this.getClass().getName());
		// Delete all file before creating the _graph
		File file =  new File(_options._indexPrefix + "/numViews.txt");
		file.delete();

		createDocumentList();

		// Write document number at the first line
		BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
		bw.write(_docList.size() + "\n");
		bw.close();

		File folder = new File(_options._logPrefix);
		File logFile = folder.listFiles()[0];
		BufferedReader br = new BufferedReader(new FileReader(logFile));
		String currentLine = null;
		while((currentLine = br.readLine()) != null){
			String[] temp = currentLine.split(" ");
			if(temp.length >= 3 && (_docList.get(temp[1]) != null) && NumberUtils.isNumber(temp[2])){
				_numViews.put(_docList.get(temp[1]), Integer.parseInt(temp[2]));
			}

			if(_numViews.size() == BULK_DOC_PROCESSING_SIZE ){
				writeFile(_numViews);
				_numViews.clear();
			}
		}
		br.close();

		if(!_numViews.isEmpty()){
			writeFile(_numViews);
			_numViews.clear();
		}

		return;
	}

	private void writeFile(Map<Integer, Integer> _numViews) throws IOException{
		String path = _options._indexPrefix + "/numViews.txt";
		File file = new File(path);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		for(Integer key : _numViews.keySet()){
			writer.write(key + " " + _numViews.get(key) + "\n");
		}
		
		writer.close();
	}

	/**
	 * During indexing mode, this function loads the NumViews values computed
	 * during mining mode to be used by the indexer.
	 * 
	 * @throws IOException
	 */
	@Override
	public Object load() throws IOException {
		System.out.println("Loading using " + this.getClass().getName());
		String path = _options._indexPrefix + "/numViews.txt";
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line = br.readLine();
		int docNum = Integer.parseInt(line);

		int[] numViewLs = new int[docNum];
		
		// read score, line: <docId> <score>
		while((line = br.readLine()) != null){
			String[] arr = line.split(" ");
			int docId = Integer.parseInt(arr[0]);
			numViewLs[docId] = Integer.parseInt(arr[1]);
		}
		br.close();
		return numViewLs;
	}
}
