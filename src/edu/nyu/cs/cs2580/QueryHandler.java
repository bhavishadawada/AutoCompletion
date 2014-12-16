package edu.nyu.cs.cs2580;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


import org.w3c.dom.CDATASection;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * Handles each incoming query, students do not need to change this class except
 * to provide more query time CGI arguments and the HTML output.
 * 
 * N.B. This class is not thread-safe. 
 * 
 * @author congyu
 * @author fdiaz
 */
class QueryHandler implements HttpHandler {

  /**
   * CGI arguments provided by the user through the URL. This will determine
   * which Ranker to use and what output format to adopt. For simplicity, all
   * arguments are publicly accessible.
   */
  public static class CgiArguments {
    // The raw user query
    public String _query = "";
    // How many results to return
    private int _numResults = 10;
    
    // The type of the ranker we will be using.
    public enum RankerType {
      NONE,
      FULLSCAN,
      CONJUNCTIVE,
      FAVORITE,
      COSINE,
      PHRASE,
      QL,
      LINEAR,
      COMPREHENSIVE,
    }
    public RankerType _rankerType = RankerType.NONE;
    
    // The output format.
    public enum OutputFormat {
      TEXT,
      HTML,
      JSON,
    }
    public OutputFormat _outputFormat = OutputFormat.TEXT;

    public int _numterms = 0;
    
    // to store userId to maintain user session query log
    public int _userId = 0 ; 
    
    public CgiArguments(String uriQuery) {
      String[] params = uriQuery.split("&");
      for (String param : params) {
        String[] keyval = param.split("=", 2);
        if (keyval.length < 2) {
          continue;
        }
        String key = keyval[0].toLowerCase();
        String val = keyval[1];
        if (key.equals("query")) {
          _query = val;
        } else if (key.equals("num")) {
          try {
            _numResults = Integer.parseInt(val);
          } catch (NumberFormatException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        } else if (key.equals("ranker")) {
          try {
            _rankerType = RankerType.valueOf(val.toUpperCase());
          } catch (IllegalArgumentException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        } else if (key.equals("format")) {
          try {
            _outputFormat = OutputFormat.valueOf(val.toUpperCase());
          } catch (IllegalArgumentException e) {
            // Ignored, search engine should never fail upon invalid user input.
          }
        } else if(key.equals("numterms")){
        	_numterms = Integer.parseInt(val);
        }
        else if(key.equals("userid")){
        	_userId = Integer.parseInt(val);
        }
      }  // End of iterating over params
    }
  }
	// For accessing the underlying documents to be used by the Ranker. Since 
	// we are not worried about thread-safety here, the Indexer class must take
	// care of thread-safety.
	private Indexer _indexer;
	
	
	public QueryHandler(Options options, Indexer indexer) {
		_indexer = indexer;
	}

	private void respondWithMsg(HttpExchange exchange, final String message)
			throws IOException {
		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "text/html");
		responseHeaders.set("Access-Control-Allow-Origin", "*");
		exchange.sendResponseHeaders(200, 0); // arbitrary number of bytes
		OutputStream responseBody = exchange.getResponseBody();
		responseBody.write(message.getBytes());
		responseBody.close();
	}

	// Function to print the expanded query at the bottom of page
	private void constructPrfOutput(
			final List<Map.Entry<String, Double>> queryEapansion, StringBuffer response) {
		JSONArray arr = new JSONArray();
		for(Map.Entry<String, Double> entry : queryEapansion){
			JSONObject obj = new JSONObject();
			obj.put("term", entry.getKey());
			arr.add(obj);
			/*response.append(response.length() > 0 ? "\n" : "");
			response.append(entry.getKey() + "\t" + entry.getValue());*/
		}
		response.append(arr.toJSONString());	
	}
	
	private void constructSuggestOutput(String[] termArr, StringBuffer response){
		JSONArray arr = new JSONArray();
		for(String term : termArr){
			JSONObject obj = new JSONObject();
			obj.put("term", term);
			arr.add(obj);
		}
		response.append(arr.toJSONString());
	}

	private void constructTextOutput(
			final Vector<ScoredDocument> docs, StringBuffer response) {
		for (ScoredDocument doc : docs) {
			response.append(response.length() > 0 ? "\n" : "");
			response.append(doc.asTextResult());
		}
		response.append(response.length() > 0 ? "\n" : "");
	}
	
	private void constructHtmlOutput(final Vector<ScoredDocument> docs, StringBuffer response){
		for (ScoredDocument doc : docs) {
			response.append(response.length() > 0 ? "\n" : "");
			response.append(doc.asHtmlResult());
		}
		response.append(response.length() > 0 ? "\n" : "");
	}
	
	private void constructJsonOutput(final Vector<ScoredDocument> docs, StringBuffer response){
		JSONArray arr = new JSONArray();
		for(ScoredDocument doc : docs){
			arr.add(doc.asJsonResult());
		}
		response.append(arr.toJSONString());
	}

	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();
		if (!requestMethod.equalsIgnoreCase("GET")) { // GET requests only.
			return;
		}

		// Print the user request header.
		Headers requestHeaders = exchange.getRequestHeaders();
		System.out.print("Incoming request: ");
		for (String key : requestHeaders.keySet()) {
			System.out.print(key + ":" + requestHeaders.get(key) + "; ");
		}
		System.out.println();

		// Validate the incoming request.
		String uriQuery = exchange.getRequestURI().getQuery();
		String uriPath = exchange.getRequestURI().getPath();
		if (uriPath == null || uriQuery == null) {
			respondWithMsg(exchange, "Something wrong with the URI!");
		}
		else if (uriPath.equals("/prf") || uriPath.equals("/search")) {
			System.out.println("Query: " + uriQuery);

			// Process the CGI arguments.
			CgiArguments cgiArgs = new CgiArguments(uriQuery);
			if (cgiArgs._query.isEmpty()) {
				respondWithMsg(exchange, "No query is given!");
			}
						
			// Create the ranker.
			Ranker ranker = Ranker.Factory.getRankerByArguments(
					cgiArgs, SearchEngine.OPTIONS, _indexer);
			if (ranker == null) {
				respondWithMsg(exchange,
						"Ranker " + cgiArgs._rankerType.toString() + " is not valid!");
			}

			// Processing the query.
			QueryPhrase processedQuery = new QueryPhrase(cgiArgs._query);
			processedQuery.processQuery();

			StringBuffer response = new StringBuffer();
			if(uriPath.equals("/prf")){
				List<Map.Entry<String, Double>> queryEapansion = ranker.psuedoRelevanceCalc(processedQuery, cgiArgs._numResults, cgiArgs._numterms);
				constructPrfOutput(queryEapansion, response);
			}
			else{
				// Ranking.
				Vector<ScoredDocument> scoredDocs =
						ranker.runQuery(processedQuery, cgiArgs._numResults);
				switch (cgiArgs._outputFormat) {
				case TEXT:
					constructTextOutput(scoredDocs, response);
					break;
				case HTML:
					// @CS2580: Plug in your HTML output
					constructHtmlOutput(scoredDocs, response);
					break;
				case JSON:
					constructJsonOutput(scoredDocs, response);
				default:
					// nothing
				}
			}
			respondWithMsg(exchange, response.toString());
			System.out.println("Finished query: " + cgiArgs._query);
			
			// Write to the user session 
			System.out.println("_userId: " + cgiArgs._userId);
			QueryLogger.addQuery(cgiArgs._userId, cgiArgs._query);
			
		}
		else if(uriPath.equals("/suggest")){
			StringBuffer response = new StringBuffer();

			// Process the CGI arguments.
			CgiArguments cgiArgs = new CgiArguments(uriQuery);
			if (cgiArgs._query.isEmpty()) {
				respondWithMsg(exchange, "No query is given!");
			}
			System.out.println("suggest prefix " + cgiArgs._query);
			Ranker ranker = Ranker.Factory.getRankerByArguments(
					cgiArgs, SearchEngine.OPTIONS, _indexer);
			
			
			
			// Write all the signals into other class and call the function here
			String[] wordArr = ranker.suggest(cgiArgs._query, 10);
			System.out.println("wordArr.length " + wordArr.length);
			for(String word : wordArr){
				System.out.println(word);
			}
			constructSuggestOutput(wordArr, response);
			respondWithMsg(exchange, response.toString());
		}
		// to return the id if the user is interacting for the first time
		else if(uriPath.equals("/getId")){
			Integer userId = QueryLogger.getId();
			System.out.println("The new userId is" + userId);
			respondWithMsg(exchange, userId.toString());
		}
		else{
			respondWithMsg(exchange, "Only /search and /prf is handled!");
		}
	}

}


