package edu.nyu.cs.cs2580;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class WordTree {
	private Node root;
	private int size;
	private int maxDepth; // Not exact, but bounding for the maximum
	private HashMap<String, Integer> dictionary;
	private ArrayList<String> termLs;
	
	public WordTree(){
		root = new Node(-1);
		size = 0;
		termLs = new ArrayList<String>();
		dictionary = new HashMap<String, Integer>();
	}
	
	public int[] wordLsToIdLs(String[] wordLs, boolean add){
		int[] idLs = new int[wordLs.length];
		for(int i = 0; i < wordLs.length; i++){
			String word = wordLs[i];
			int id;
			if(dictionary.containsKey(word)){
				id = dictionary.get(word);
			}
			else if(add){
				id = dictionary.size();
				dictionary.put(word, id);
				termLs.add(word);
			}
			else{
				id = -1;
			}
			idLs[i] = id;
		}
		return idLs;
	}
	
	public String[] idLsToWordLs(int[] idLs){
		String[] wordLs = new String[idLs.length];
		for(int i = 0; i < idLs.length; i++){
			int id = idLs[i];
			if(id < 0)
				return new String[0];
			else
				wordLs[i] = termLs.get(id);
		}
		return wordLs;
	}
	
	public boolean add(String[] wordLs){
		if (wordLs.length == 0)
			throw new IllegalArgumentException("wordLs can't be empty");
		int[] idLs = wordLsToIdLs(wordLs, true);
		return add(idLs);
	}
	
	public boolean add(int[] idLs)
	{
		if (idLs.length == 0)
			throw new IllegalArgumentException("idLs can't be empty");
		if (add(root, idLs, 0))
		{
			size++;
			int n = idLs.length;
			if (n > maxDepth) maxDepth = n;
			return true;
		}
		return false;
	}
	
	public void addAll(int[][] idArr)
	{
		for (int[] idLs: idArr)
			add(idLs);
	}
	
	private boolean add(Node root, int[] idLs, int offset)
	{
		if (offset == idLs.length) return false;
		int c = idLs[offset];

		// Search for node to add to
		Node last = null, next = root.firstChild;
		while (next != null)
		{
			if (next.id < c)
			{
				// Not found yet, continue searching
				last = next;
				next = next.nextSibling;
			}
			else if (next.id == c)
			{
				// Match found, add remaining word to this node
				return add(next, idLs, offset + 1);
			}
			// Because of the ordering of the list getting here means we won't
			// find a match
			else break;
		}

		// No match found, create a new node and insert
		Node node = new Node(c);
		if (last == null)
		{
			// Insert node at the beginning of the list (Works for next == null
			// too)
			root.firstChild = node;
			node.nextSibling = next;
		}
		else
		{
			// Insert between last and next
			last.nextSibling = node;
			node.nextSibling = next;
		}

		// Add remaining letters
		for (int i = offset + 1; i < idLs.length; i++)
		{
			node.firstChild = new Node(idLs[i]);
			node = node.firstChild;
		}
		return true;
	}
	
	private void getAll(Node root, ArrayList<int[]> idArr, int[] idLs, int pointer)
	{
		Node n = root.firstChild;
		while (n != null)
		{
			idLs[pointer] = n.id;
			if (n.firstChild == null)
			{
				idArr.add(Arrays.copyOfRange(idLs, 0, pointer+1));
			}
			else
			{
				getAll(n, idArr, idLs, pointer + 1);
			}
			n = n.nextSibling;
		}
	}

	public int size()
	{
		return size;
	}
	
	public String[][] suggest(String[] wordLs){
		int[] idLs = wordLsToIdLs(wordLs, false);
		int[][] idArr = suggest(idLs);
		ArrayList<String[]> wordArr = new ArrayList<String[]>();
		for(int i = 0; i < idArr.length; i++){
			String[] strLs = idLsToWordLs(idArr[i]);
			if(strLs.length > 0){
				wordArr.add(strLs);
			}
		}
		return wordArr.toArray(new String[wordArr.size()][]);
	}
	
	public int[][] suggest(int[] idLs)
	{
		return suggest(root, idLs, 0);
	}
	
	private int[][] suggest(Node root, int[] idLs, int offset)
	{
		if (offset == idLs.length)
		{
			ArrayList<int[]> idArr = new ArrayList<int[]>(size);
			int[] newIdLs = new int[maxDepth];
			for (int i = 0; i < offset; i++)
				newIdLs[i] = idLs[i];
			getAll(root, idArr, newIdLs, offset);
			return idArr.toArray(new int[idArr.size()][]);
		}
		int c = idLs[offset];

		// Search for node to add to
		Node next = root.firstChild;
		while (next != null)
		{
			if (next.id < c) next = next.nextSibling;
			else if (next.id == c) return suggest(next, idLs, offset + 1);
			else break;
		}
		return new int[][] { idLs};
	}

	
	private class Node
	{
		public int id;
		public Node firstChild;
		public Node nextSibling;

		public Node(int id)
		{
			this.id = id;
			firstChild = null;
			nextSibling = null;
		}
	}
	
	static public void main(String[] args){
		WordTree wt = new WordTree();
		wt.add(new String[] {"new", "york"});
		wt.add(new String[] {"new", "york", "city"});
		wt.add(new String[] {"new", "year"});
		wt.add(new String[] {"new", "shoes"});
		String[][] wordArr = wt.suggest(new String[] {"new"});
		for(String[] wordLs : wordArr){
			for(String word : wordLs){
				System.out.print(word + " ");
			}
			System.out.println();
		}
	}
}
