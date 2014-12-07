package edu.nyu.cs.cs2580;

public class TrieTree {

	private TrieNode root = null; 
	
	// constructor to create the tree
	public TrieTree(){
	}
	
	public static void inserWord(TrieNode root , String word){
		int length = word.length() ; 
		char[] letters = word.toLowerCase().toCharArray();
		TrieNode currentNode = root;
		
		for(int i = 0; i < letters.length ; i++){
			if(currentNode.links[letters[i]] == null){
				currentNode.links[letters[i]] = new TrieNode(letters[i]);
				currentNode = currentNode.links[letters[i]];
			}
			currentNode.fullWord = true;
		}
	}
	
	

}

class TrieNode{
	char letter ; 
	TrieNode[] links; 
	boolean fullWord;
	
	 TrieNode(char letter){
		 this.letter = letter;
		 links = new TrieNode[26];
		 this.fullWord = false;
	 }
}
