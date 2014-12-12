package edu.nyu.cs.cs2580;
import org.json.simple.JSONObject;

/**
 * Document with score.
 * 
 * @author fdiaz
 * @author congyu
 */
class ScoredDocument implements Comparable<ScoredDocument> {
  Document _doc;
  double _score;

  public ScoredDocument(Document doc, double score) {
    _doc = doc;
    _score = score;
  }

  public String asTextResult() {
    StringBuffer buf = new StringBuffer();
    buf.append(_doc._docid).append("\t");
    buf.append(_doc.getTitle()).append("\t");
    buf.append(_doc._pageRank).append("\t");
    buf.append(_doc._numViews).append("\t");
    buf.append(_score);
    return buf.toString();
  }

  /**
   * @CS2580: Student should implement {@code asHtmlResult} for final project.
   */
  public String asHtmlResult() {
		StringBuilder sb = new StringBuilder();
		if(_doc != null){
			sb.append("<!DOCTYPE html><html><head></head><body>");
			sb.append("<h1>");
			sb.append(_doc._docid + "\t");
			sb.append(_doc.getTitle() + "\t");
			sb.append(_doc._pageRank + "\t");
			sb.append(_doc._numViews + "\t");
			sb.append(_score + "\t");
			sb.append("</h1><br>");
			sb.append("</body></html>");
		}
		return sb.toString();
  }
  
  public JSONObject asJsonResult(){
	  JSONObject obj = new JSONObject(); 
	  obj.put("title", _doc._title);
	  obj.put("docid", _doc._docid);
	  obj.put("url", _doc._url);
	  return obj;
  }
  
  public Document getDocument(){
	  return _doc;
  }
  @Override
  public int compareTo(ScoredDocument o) {
    if (this._score == o._score) {
      return 0;
    }
    return (this._score > o._score) ? 1 : -1;
  }
  
  
}
