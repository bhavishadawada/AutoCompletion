package edu.nyu.cs.cs2580;

import java.util.Vector;

import edu.nyu.cs.cs2580.QueryHandler.CgiArguments;
import edu.nyu.cs.cs2580.SearchEngine.Options;

/**
 * @CS2580: Implement this class for HW3 based on your {@code RankerFavorite}
 * from HW2. The new Ranker should now combine both term features and the
 * document-level features including the PageRank and the NumViews. 
 */
public class RankerComprehensive extends Ranker {
	final double betaScore    = 0.8;
	final double betaNumViews = 0.01;
	final double betaPageRank = 0.09;

  public RankerComprehensive(Options options,
      CgiArguments arguments, Indexer indexer) {
    super(options, arguments, indexer);
    System.out.println("Using Ranker: " + this.getClass().getSimpleName());
  }

  @Override
  public ScoredDocument runquery(Query query, Document doc) {
	ScoredDocument sd = new RankerFavorite(_options, _arguments, _indexer).runquery(query, doc);
	sd._score = betaScore*sd._score + betaNumViews*normalize(sd._doc._numViews) + betaPageRank*sd._doc._pageRank;
	return sd;
  }

  public double normalize(double in){
      double out = 1 - Math.pow(Math.E, -in);
      return out;
  }
}
