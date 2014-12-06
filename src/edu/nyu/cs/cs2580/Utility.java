package edu.nyu.cs.cs2580;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * This class contains all utility methods
 * @author bdawada
 *
 */
public class Utility {
	// This can then help removing stop words
	/*public static Set<String> tokenize(String document){
		Set<String> uniqueTermSet = new HashSet<String>();
		
		String str;
        Pattern ptn = Pattern.compile("([^a-zA-Z0-9])");
        str = ptn.matcher(document).replaceAll(" $1 ");

		StringTokenizer st = new StringTokenizer(str);
		while(st.hasMoreTokens()){
			String token = st.nextToken().toLowerCase().trim();
			if(token.length() > 0){
				uniqueTermSet.add(token);
			}
		}
		return uniqueTermSet;
	}*/

	/*public static Vector<String> tokenize2(String document){
		Vector<String> tokenVec = new Vector<String>();
		
		String str;
        Pattern ptn = Pattern.compile("([^a-zA-Z0-9])");
        str = ptn.matcher(document).replaceAll(" $1 ");

		StringTokenizer st = new StringTokenizer(str);
		while(st.hasMoreTokens()){
			String token = st.nextToken().toLowerCase().trim();
			if(token.length() > 0){
				tokenVec.add(token);
			}
		}
		return tokenVec;
	}*/
	
	// To make stem words
		/*public static List<String> tokenize2(String input){
			List<String> tempTokens = new ArrayList<String>();
			TokenStream stream = analyze(input);
			CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
			try {
				while (stream.incrementToken()) {
					String stemmedToken = cattr.toString().trim();
					if (stemmedToken.matches("[a-zA-Z0-9']*")) {
						stemmedToken = Stemmer.getStemmedWord(stemmedToken
								.toLowerCase());
						tempTokens.add(stemmedToken);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				stream.end();
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return tempTokens;
		}*/
		
	public static List<String> tokenize2(String input) {
		List<String> tempTokens = new ArrayList<String>();
		Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_36,
	            new StringReader(input));

	    final StandardFilter standardFilter = new StandardFilter(Version.LUCENE_36, tokenizer);
	    final StopFilter stopFilter = new StopFilter(Version.LUCENE_36, standardFilter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		CharTermAttribute cattr = tokenizer.addAttribute(CharTermAttribute.class);
		
		try {
			while (stopFilter.incrementToken()) {
				String stemmedToken = cattr.toString().trim();
				if (stemmedToken.matches("[a-zA-Z0-9']*")) {
					stemmedToken = Stemmer.getStemmedWord(stemmedToken
							.toLowerCase());
					tempTokens.add(stemmedToken);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			stopFilter.reset();
			stopFilter.end();
			stopFilter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tempTokens;
	}
		public static Set<String> tokenize(String input){
			Set<String> tempTokens = new HashSet<String>();
			Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_36,
		            new StringReader(input));

		    final StandardFilter standardFilter = new StandardFilter(Version.LUCENE_36, tokenizer);
		    final StopFilter stopFilter = new StopFilter(Version.LUCENE_36, standardFilter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
			CharTermAttribute cattr = tokenizer.addAttribute(CharTermAttribute.class);
			
			try {
				while (stopFilter.incrementToken()) {
					String stemmedToken = cattr.toString().trim();
					if (stemmedToken.matches("[a-zA-Z0-9']*")) {
						stemmedToken = Stemmer.getStemmedWord(stemmedToken
								.toLowerCase());
						tempTokens.add(stemmedToken);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				stopFilter.reset();
				stopFilter.end();
				stopFilter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return tempTokens;
		}
		
		// To remove stop words
		private static TokenStream analyze(String input) {
			Set<String> set = new HashSet<String>();
			set.add("a");
			set.add("b");
			Analyzer an = new EnglishAnalyzer(Version.LUCENE_30, set);
			TokenStream stream = an
					.tokenStream("FileName", new StringReader(input));
			an.close();
			return stream;
		}

	public static List<String> getFilesInDirectory(String directory) {
		File folder = new File(directory);
		List<String> files = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			files.add(fileEntry.getName());
		}
		System.out.println(files.size());
		return files;
	}
	
	public static void main(String[] args) {
		//String doc = "This is to test set to";
		//tokenize(doc);
		getFilesInDirectory("data/simple");
	}
}

