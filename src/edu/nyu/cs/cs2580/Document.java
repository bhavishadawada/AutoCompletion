package edu.nyu.cs.cs2580;

import java.io.Serializable;

/**
 * The basic implementation of a Document.  Only the most basic information are
 * maintained in this class. Subclass should implement additional information
 * for display or ranking, such as snippet, term vectors, anchors, etc.
 * 
 * In HW1: instructors provide {@link DocumentFull}.
 * 
 * In HW2: student must implement the more efficient {@link DocumentIndexed}.
 * 
 * @author fdiaz
 * @author congyu
 */
class Document implements Serializable {
  private static final long serialVersionUID = -539495106357836976L;

  public int _docid;

  // Basic information for display
  public String _title = "";
  public String _url = "";
  
  int[] termId;
  int[] termFrequency;
  // Basic information for ranking
  float _pageRank = 0.0f;
  int _numViews = 0;

  public Document(int docid) {
    _docid = docid;
  }

  public String getTitle() {
    return _title;
  }

  public void setTitle(String title) {
    this._title = title;
  }

  public String getUrl() {
    return _url;
  }

  public void setUrl(String url) {
    this._url = url;
  }

  public float getPageRank() {
    return _pageRank;
  }

  public void setPageRank(float pageRank) {
    this._pageRank = pageRank;
  }

  public int getNumViews() {
    return _numViews;
  }

  public void setNumViews(int numViews) {
    this._numViews = numViews;
  }
  
}
