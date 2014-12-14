package edu.nyu.cs.cs2580;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

public class WordTree {
	private Node root;
	private int size;
	private int maxDepth; // Not exact, but bounding for the maximum
	private HashMap<String, Integer> dictionary;
	private ArrayList<String> termLs;

	public WordTree(HashMap<String, Integer> dictionary, ArrayList<String> termLs){
		root = new Node(-1);
		size = 0;
		this.dictionary = dictionary;
		this.termLs = termLs;
	}

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

	public boolean add(String query){
		// add an extra node with "\t" at the end
		// such that the tree can suggest both "new york" and "new york city" 
		query = query + " \t";
		return add(query.split(" "));
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
		root.freq++;
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
		node.freq++;
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
			node.freq++;
		}
		
		return true;
	}

	private void getAll(Node root, ArrayList<Item> itemLs, int[] idLs, int pointer)
	{
		Node n = root.firstChild;
		while (n != null)
		{
			if (n.firstChild == null)
			{
				itemLs.add(new Item(n.freq, Arrays.copyOfRange(idLs, 0, pointer)));
			}
			else
			{
				idLs[pointer] = n.id;
				getAll(n, itemLs, idLs, pointer + 1);
			}
			n = n.nextSibling;
		}
	}

	public int size()
	{
		return size;
	}

	public List<String> suggest(String query){
		String[] wordLs = query.split(" ");
		String[][] wordArr = suggest(wordLs);
		List<String> suggestLs = new ArrayList<String>(wordArr.length);
		for(int i = 0; i < wordArr.length; i++){
			StringBuffer sb = new StringBuffer();
			String[] ls = wordArr[i];
			for(int j = 0; j < ls.length; j++){
				if(j > 0)
					sb.append(" ");
				sb.append(ls[j]);
			}
			suggestLs.add(sb.toString());
		}
		return suggestLs;
	}

	/*
	 * Recursive function for finding all words starting with the given prefix
	 */

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
			ArrayList<Item> itemLs = new ArrayList<Item>(size);
			int[] newIdLs = new int[maxDepth];
			for (int i = 0; i < offset; i++)
				newIdLs[i] = idLs[i];
			getAll(root, itemLs, newIdLs, offset);
			Collections.sort(itemLs);
			int[][] idArr = new int[itemLs.size()][];
			for(int i = 0; i < itemLs.size(); i++){
				idArr[i] = itemLs.get(i).idLs;
			}
			return idArr;
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
		public int freq;
		public Node firstChild;
		public Node nextSibling;

		public Node(int id)
		{
			this.id = id;
			freq = 0;
			firstChild = null;
			nextSibling = null;
		}
	}

	private class Item implements Comparable{
		public int freq;
		int[] idLs;
		public Item(int freq, int[] idLs){
			this.freq = freq;
			this.idLs = idLs;
		}
		@Override
		public int compareTo(Object o) {
			// TODO Auto-generated method stub
			return ((Item)o).freq - this.freq;
		}

		public String toString(){
			StringBuffer sb = new StringBuffer();
			sb.append(freq);
			for(int id : idLs){
				sb.append(" ");
				sb.append(termLs.get(id));
			}
			return sb.toString();
		}

	}
	
	public static WordTree genTree(String str){
		String[] ls = str.split(" ");
		int i = 0;
		WordTree wt = new WordTree();
		while(i < ls.length){
			int freq = Integer.parseInt(ls[i]);
			i++;
			int len = Integer.parseInt(ls[i]);
			i++;
			System.out.println(freq + " " + len);
			int[] idLs = new int[len];
			for(int j = 0; j < len; j++){
				idLs[j] = Integer.parseInt(ls[i]);
				i++;
			}
			wt.add(idLs);
		}
		return wt;
	}
	
	public String convertTreeToString(int threshold){
		StringBuffer out = new StringBuffer();
		int[] idLs = new int[maxDepth];
		nodeToString(root, out, idLs, 0, "", " ", threshold);
		return out.toString();
	}
	
	private static void nodeToString(Node root, StringBuffer out, int[] idLs, int pointer, String pre, String post, int threshold){
		Node n = root.firstChild;
		while (n != null)
		{
			if(n.freq < threshold){
				//trim
			}
			else if (n.firstChild == null)
			{
				// freq len idLS
				out.append(pre);
				out.append(Integer.toString(n.freq));
				out.append(" ");
				out.append(Integer.toString(pointer));
				out.append(" ");
				for(int i = 0; i < pointer; i++){
					if(i > 0)
						out.append(" ");
					out.append(Integer.toString(idLs[i]));
				}
				out.append(post);
			}
			else
			{
				idLs[pointer] = n.id;
				nodeToString(n, out, idLs, pointer + 1, pre, post, threshold);
			}
			n = n.nextSibling;
		}
	}
	
	public void writeFile(String file, String delimiter, String quoteMark) throws IOException
	{
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		int[] idLs = new int[maxDepth];
		writeNode(root, out, idLs, 0, quoteMark, quoteMark + delimiter);
		out.close();
	}

	private static void writeNode(Node root, java.io.BufferedWriter out, int[] idLs, int pointer, String pre, String post) throws java.io.IOException
	{
		Node n = root.firstChild;
		while (n != null)
		{
			if (n.firstChild == null)
			{
				out.write(pre);
				out.write(Integer.toString(n.freq));
				out.write("\t");
				for(int i = 0; i < pointer; i++){
					if(i > 0)
						out.write(" ");
					out.write(Integer.toString(idLs[i]));
				}
				out.write(post);
			}
			else
			{
				idLs[pointer] = n.id;
				writeNode(n, out, idLs, pointer + 1, pre, post);
			}
			n = n.nextSibling;
		}
	}
	

	static public void main(String[] args) throws IOException{
		WordTree wt = new WordTree();
		wt.add("new york");
		wt.add("new york");
		wt.add("new york city");
		wt.add("new york city");
		wt.add("new york city");
		wt.add("new york city");
		wt.add("new year");
		wt.add("new shoes");
		List<String> wordArr = wt.suggest("new");
		for(String wordLs : wordArr){
			System.out.println(wordLs);
		}
		System.out.println(wt.convertTreeToString(2));
		WordTree wt2 = genTree(wt.convertTreeToString(2));
		System.out.println(wt2.convertTreeToString(0));
	}
}
