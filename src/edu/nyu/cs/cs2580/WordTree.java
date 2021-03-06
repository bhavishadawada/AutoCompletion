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
	static HashMap<String, Integer> dictionary;
	static ArrayList<String> termLs;

	/*
	public WordTree(HashMap<String, Integer> dictionary, ArrayList<String> termLs){
		root = new Node(-1);
		size = 0;
		this.dictionary = dictionary;
		this.termLs = termLs;
	}
	*/
	
	public static void init(HashMap<String, Integer> dictionary, ArrayList<String> termLs){
		WordTree.dictionary = dictionary;
		WordTree.termLs = termLs;
	}

	public WordTree(){
		root = new Node(-1);
		size = 0;
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
		/*
		// add an extra node with "\t" at the end
		// such that the tree can suggest both "new york" and "new york city" 
		query = query + " \t";
		*/
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
		if (add(root, idLs, 0, 1))
		{
			size++;
			int n = idLs.length;
			if (n > maxDepth) maxDepth = n;
			return true;
		}
		return false;
	}

	public boolean add(int[] idLs, int freq)
	{
		if (idLs.length == 0)
			throw new IllegalArgumentException("idLs can't be empty");
		if (add(root, idLs, 0, freq))
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

	private boolean add(Node root, int[] idLs, int offset, int freq)
	{
		root.freq += freq;
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
				return add(next, idLs, offset + 1, freq);
			}
			// Because of the ordering of the list getting here means we won't
			// find a match
			else break;
		}

		// No match found, create a new node and insert
		Node node = new Node(c);
		node.freq += freq;
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
			node.freq += freq;
		}

		return true;
	}

	private void getAll(Node root, ArrayList<Item> itemLs, int[] idLs, int pointer)
	{
		Node n = root.firstChild;
		while (n != null)
		{
			idLs[pointer] = n.id;
			itemLs.add(new Item(n.freq, Arrays.copyOfRange(idLs, 0, pointer + 1)));
			getAll(n, itemLs, idLs, pointer + 1);
			n = n.nextSibling;
		}
	}

	public int size()
	{
		return size;
	}

	public ArrayList<Suggest> suggest(String query){
		String[] wordLs = query.split(" ");
		return suggest(wordLs);
	}

	/*
	 * Recursive function for finding all words starting with the given prefix
	 */
	public double normalize(double in, double threshold){
	    double out = 1 - Math.pow(Math.E, -in/threshold);
	    return out;
	}

	public ArrayList<Suggest> suggest(String[] wordLs){
		int[] idLs = wordLsToIdLs(wordLs, false);
		ArrayList<Item>  idArr = suggest(idLs);
		ArrayList<Suggest> sgLs = new ArrayList<Suggest>();
		for(int i = 0; i < idArr.size(); i++){
			String[] strLs = idLsToWordLs(idArr.get(i).idLs);
			if(strLs.length > 0){
				sgLs.add(new Suggest(normalize(idArr.get(i).freq, 10)/2, strLs));
			}
		}
		return sgLs;
	}

	public ArrayList<Item> suggest(int[] idLs)
	{
		return suggest(root, idLs, 0);
	}

	private ArrayList<Item> suggest(Node root, int[] idLs, int offset)
	{
		ArrayList<Item> itemLs = new ArrayList<Item>();
		if (offset == idLs.length)
		{
			int[] newIdLs = new int[maxDepth];
			for (int i = 0; i < offset; i++)
				newIdLs[i] = idLs[i];
			getAll(root, itemLs, newIdLs, offset);
			Collections.sort(itemLs);
			return itemLs;
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
		//return new int[][] { idLs};
		return itemLs;
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
		WordTree wt = new WordTree();
		mergeTree(wt, str);
		return wt;
	}

	// To merge to tree while reading from the file
	public static void mergeTree(WordTree wt, String str){
		String[] ls = str.split(" ");
		int i = 0;
		while(i < ls.length){
			if(ls[i].length() == 0)
				break; // ignore empty string
			int freq = Integer.parseInt(ls[i].trim());
			i++;
			int len = Integer.parseInt(ls[i].trim());
			i++;
			int[] idLs = new int[len];
			for(int j = 0; j < len; j++){
				idLs[j] = Integer.parseInt(ls[i].trim());
				i++;
			}
			wt.add(idLs, freq);
		}
	}

	// Convert Tree to string for serializing 
	public String convertTreeToString(int threshold){
		StringBuffer out = new StringBuffer();
		int[] idLs = new int[maxDepth];
		trim(root, threshold);
		nodeToString(root, out, idLs, 0, "", " ", 0);
		return out.toString();
	}
	
	public void trim(int threshold){
		
	}
	private static void trim(Node root, int threshold){
		if(root.firstChild == null){
			return;
		}
		else{
			Node last = null;
			Node node = root.firstChild;
			while(node != null){
				if(node.freq < threshold){
					//System.out.println("trim " + node.id);
					if(last == null){
						root.firstChild = node.nextSibling;
					}
					else{
						last.nextSibling = node.nextSibling;
					}
				}
				else{
					trim(node, threshold);
					last = node;
				}
				node = node.nextSibling;
			}
		}
	}

	private static void nodeToString(Node root, StringBuffer out, int[] idLs, int pointer, String pre, String post, int threshold){
		Node n = root.firstChild;
		while (n != null)
		{
			idLs[pointer] = n.id;
			if(n.freq < threshold){
				//trim
			}
			else if (n.firstChild == null)
			{
				// freq len idLS
				out.append(pre);
				out.append(Integer.toString(n.freq));
				out.append(" ");
				out.append(Integer.toString(pointer+1));
				out.append(" ");
				for(int i = 0; i <= pointer; i++){
					if(i > 0)
						out.append(" ");
					out.append(Integer.toString(idLs[i]));
				}
				out.append(post);
			}
			else
			{
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

					out.write(post);
				}
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
		WordTree.init(new HashMap<String, Integer>(), new ArrayList<String>());
		WordTree wt = new WordTree();
		wt.add("new york indian");
		wt.add("new york taiwan");
		wt.add("new york city");
		wt.add("new york city");
		wt.add("new york city");
		wt.add("new york times");
		wt.add("new york happy");
		List<Suggest> sgLs = wt.suggest("new");
		for(Suggest sg : sgLs){
			System.out.println(sg);
		}
		String str = wt.convertTreeToString(0);
		System.out.println(str);
		//WordTree.trim(wt.root, 3);
		str = wt.convertTreeToString(3);
		System.out.println(str);

		sgLs = wt.suggest("new");
		for(Suggest sg : sgLs){
			System.out.println(sg);
		}

		str = wt.convertTreeToString(0);
		System.out.println(str);

		WordTree wt2 = genTree(str);
		System.out.println(wt2.convertTreeToString(0));
	}
}
