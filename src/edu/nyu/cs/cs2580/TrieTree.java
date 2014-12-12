import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/*
	Copyright 2010 Michael Clerx
	work@michaelclerx.com

    This file is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This file is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this file.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Represents an ordered list of words used for no other task than to look up if a word is on the
 * list. The words are sorted by Unicode value.
 * 
 * Note: The structure uses a delimiter to signal the end of words internally, this means the
 * delimiter may never occur in the words passed to the list. This is the user's own
 * responsibility.
 * 
 * This data structure is not thread safe in any way.
 */
class TrieTree
{
	/**
	 * The delimiter used in this word to tell where words end. Without a proper delimiter either A.
	 * a lookup for 'win' would return false if the list also contained 'windows', or B. a lookup
	 * for 'mag' would return true if the only word in the list was 'magnolia'
	 * 
	 * The delimiter should never occur in a word added to the trie.
	 */
	public final static char DELIMITER = '\u0001';

	/**
	 * Creates a new Trie.
	 * @param ignoreCase Set this to true to make the trie ignore case (warning: this slows down
	 *            performance considerably!)
	 */
	public TrieTree(boolean ignoreCase)
	{
		root = new Node('r');
		size = 0;
		this.ignoreCase = false;
	}

	/**
	 * Adds a word to the list.
	 * @param word The word to add.
	 * @return True if the word wasn't in the list yet
	 */
	public boolean add(String word)
	{
		if (word.length() == 0)
			throw new IllegalArgumentException("Word can't be empty");
		String w = (ignoreCase) ? word.toLowerCase() : word;
		if (add(root, w + DELIMITER, 0))
		{
			size++;
			int n = word.length();
			if (n > maxDepth) maxDepth = n;
			return true;
		}
		return false;
	}

	/**
	 * Adds an array of words to the list.
	 * @param word The words to add.
	 */
	public void addAll(String[] words)
	{
		for (String word : words)
			add(word);
	}

	/*
	 * Does the real work of adding a word to the trie
	 */
	private boolean add(Node root, String word, int offset)
	{
		if (offset == word.length()) return false;
		int c = word.charAt(offset);

		// Search for node to add to
		Node last = null, next = root.firstChild;
		while (next != null)
		{
			if (next.value < c)
			{
				// Not found yet, continue searching
				last = next;
				next = next.nextSibling;
			}
			else if (next.value == c)
			{
				// Match found, add remaining word to this node
				return add(next, word, offset + 1);
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
		for (int i = offset + 1; i < word.length(); i++)
		{
			node.firstChild = new Node(word.charAt(i));
			node = node.firstChild;
		}
		return true;
	}

	/**
	 * Removes a word from the list.
	 * @param word The word to remove.
	 * @return True if the word was found and removed.
	 */
	public boolean remove(String word)
	{
		if (word.length() == 0)
			throw new IllegalArgumentException("Word can't be empty");
		String w = (ignoreCase) ? word.toLowerCase() : word;
		if (remove(root, w + DELIMITER, 0, null, null, null))
		{
			size--;
			return true;
		}
		return false;
	}

	/*
	 * Removes a word from the list: searches for the word while retaining information about the
	 * last time it encountered a tree branch, and which branch of the tree it followed...
	 */
	private boolean remove(Node root, String word, int offset, Node branch, Node branchLast, Node branchNext)
	{
		if (offset == word.length())
		{
			// Word found, delete entry at last branch
			if (branch == null)
			{
				// No branches found in the tree, only one word!
				this.root.firstChild = null;
			}
			else
			{
				if (branchLast == null) branch.firstChild = branchNext;
				else branchLast.nextSibling = branchNext;
			}
			return true;
		}

		// Search for word
		int c = word.charAt(offset);
		Node last = null, next = root.firstChild;
		while (next != null)
		{
			if (next.value < c)
			{
				last = next;
				next = next.nextSibling;
			}
			else if (next.value == c)
			{
				// Test if this node had more than one child
				if (last != null || next.nextSibling != null)
				{
					branch = root;
					branchLast = last;
					branchNext = next.nextSibling;
				}
				return remove(next, word, offset + 1, branch, branchLast,
					branchNext);
			}
			else return false;
		}
		return false;
	}

	/**
	 * Searches for a word in the list.
	 * 
	 * @param word The word to search for.
	 * @return True if the word was found.
	 */
	public boolean isEntry(String word)
	{
		if (word.length() == 0)
			throw new IllegalArgumentException("Word can't be empty");
		String w = (ignoreCase) ? word.toLowerCase() : word;
		return isEntry(root, w + DELIMITER, 0);
	}

	/*
	 * Does the real work of determining if a word is in the list
	 */
	private boolean isEntry(Node root, String word, int offset)
	{
		if (offset == word.length()) return true;
		int c = word.charAt(offset);

		// Search for node to add to
		Node next = root.firstChild;
		while (next != null)
		{
			if (next.value < c) next = next.nextSibling;
			else if (next.value == c) return isEntry(next, word, offset + 1);
			else return false;
		}
		return false;
	}

	/**
	 * Returns all words in the list. If (ignoreCase=true) all words are returned in lower case. For
	 * large lists this will be a fairly slow operation.
	 * 
	 * @return A String array of all words in the list
	 */
	public String[] getAll()
	{
		ArrayList<String> words = new ArrayList<String>(size);
		char[] chars = new char[maxDepth];
		getAll(root, words, chars, 0);
		return words.toArray(new String[size]);
	}

	/*
	 * Adds any words found in this branch to the array
	 */
	private void getAll(Node root, ArrayList<String> words, char[] chars, int pointer)
	{
		Node n = root.firstChild;
		while (n != null)
		{
			if (n.firstChild == null)
			{
				words.add(new String(chars, 0, pointer));
			}
			else
			{
				chars[pointer] = (char)n.value;
				getAll(n, words, chars, pointer + 1);
			}
			n = n.nextSibling;
		}
	}

	/**
	 * Returns the size of this list;
	 */
	public int size()
	{
		return size;
	}

	/**
	 * Returns all words in this list starting with the given prefix
	 * 
	 * @param prefix The prefix to search for.
	 * @return All words in this list starting with the given prefix, or if no such words are found,
	 *         an array containing only the suggested prefix.
	 */
	public String[] suggest(String prefix)
	{
		return suggest(root, prefix, 0);
	}

	/*
	 * Recursive function for finding all words starting with the given prefix
	 */
	private String[] suggest(Node root, String word, int offset)
	{
		if (offset == word.length())
		{
			ArrayList<String> words = new ArrayList<String>(size);
			char[] chars = new char[maxDepth];
			for (int i = 0; i < offset; i++)
				chars[i] = word.charAt(i);
			getAll(root, words, chars, offset);
			return words.toArray(new String[words.size()]);
		}
		int c = word.charAt(offset);

		// Search for node to add to
		Node next = root.firstChild;
		while (next != null)
		{
			if (next.value < c) next = next.nextSibling;
			else if (next.value == c) return suggest(next, word, offset + 1);
			else break;
		}
		return new String[] { word };
	}

	/**
	 * Returns a clone of this trie
	 * @return A clone of this trie
	 */
	public TrieTree clone()
	{
		TrieTree clone = new TrieTree(ignoreCase);
		cloneKids(root, clone.root);
		clone.size = size;
		clone.maxDepth = maxDepth;
		return clone;
	}

	// Recursively clones the children of a node
	private void cloneKids(Node root, Node rootClone)
	{
		Node n = root.firstChild;
		Node nClone = null, insertAfter = null;
		while (n != null)
		{
			// Insert copy of node into clone
			nClone = new Node(n.value);
			if (insertAfter == null) rootClone.firstChild = nClone;
			else insertAfter.nextSibling = nClone;

			// Repeat for child node
			if (n.firstChild != null) cloneKids(n, nClone);

			// Move to next sibling
			n = n.nextSibling;
			insertAfter = nClone;
		}
	}

	/**
	 * Searches a string for words present in the trie and replaces them with stars (asterixes).
	 * @param z The string to censor
	 */
	public String censor(String s)
	{
		if (size == 0) return s;		
		String z = s.toLowerCase();		
		int n = z.length();
		StringBuilder buffer = new StringBuilder(n);
		int match;
		char star = '*';
		for (int i = 0; i < n;)
		{
			match = longestMatch(root, z, i, 0, 0);
			if (match > 0)
			{
				for (int j = 0; j < match; j++)
				{
					buffer.append(star);
					i++;
				}
			}
			else
			{
				buffer.append(s.charAt(i++));
			}
		}
		return buffer.toString();

	}

	/*
	 * Finds the longest matching word in the trie that starts at the given offset...
	 */
	private int longestMatch(Node root, String word, int offset, int depth, int maxFound)
	{
		// Uses delimiter = first in the list!
		Node next = root.firstChild;
		if (next.value == DELIMITER) maxFound = depth;
		if (offset == word.length()) return maxFound;
		int c = word.charAt(offset);

		while (next != null)
		{
			if (next.value < c) next = next.nextSibling;
			else if (next.value == c) return longestMatch(next, word,
				offset + 1, depth + 1, maxFound);
			else return maxFound;
		}
		return maxFound;
	}

	/*
	 * Represents a node in the trie. Because a node's children are stored in a linked list this
	 * data structure takes the odd structure of node with a firstChild and a nextSibling.
	 */
	private class Node
	{
		public int value;
		public Node firstChild;
		public Node nextSibling;

		public Node(int value)
		{
			this.value = value;
			firstChild = null;
			nextSibling = null;
		}
	}

	/**
	 * Reads words from a file into the trie. The file is read one line at a time, a line may
	 * contain multiple words separated by whitespace.
	 * 
	 * @param file The file to read
	 * @param strip1 When added the file will replace any instances of 'strip' from the file. This
	 *            can be used to remove quotes or turn commas into spaces.
	 * @param strip2 Like strip1
	 */
	public void addFile(String file, String strip1, String strip2) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		String[] words;
		Pattern whitespace = Pattern.compile("\\s+");
		Pattern quote1 = Pattern.compile(strip1);
		Pattern quote2 = Pattern.compile(strip2);
		String empty = " ";
		while ((line = in.readLine()) != null)
		{
			line = quote1.matcher(line).replaceAll(empty);
			line = quote2.matcher(line).replaceAll(empty);
			words = whitespace.split(line);
			for (String w : words)
				if (w.length() > 0) add(w);
		}
		in.close();
	}

	/**
	 * Writes this trie's contents to a file
	 * @param file The path to write to
	 * @param delimiter Every word printed is followed by this delimiter
	 * @param quotes Every word printed is wrapped in this string
	 */
	public void writeFile(String file, String delimiter, String quoteMark) throws IOException
	{
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		char[] c = new char[maxDepth];
		writeNode(root, out, c, 0, quoteMark, quoteMark + delimiter);
		out.close();
	}

	private static void writeNode(Node root, java.io.BufferedWriter out, char[] chars, int pointer, String pre, String post) throws java.io.IOException
	{
		Node n = root.firstChild;
		while (n != null)
		{
			if (n.firstChild == null)
			{
				out.write(pre);
				out.write(new String(chars, 0, pointer));
				out.write(post);
			}
			else
			{
				chars[pointer] = (char)n.value;
				writeNode(n, out, chars, pointer + 1, pre, post);
			}
			n = n.nextSibling;
		}
	}
	
	/*
	public static void main(String[] args)
	{
		Trie t = new Trie();
		System.out.println(t.remove("test"));
		System.out.println(t.isEntry("isEntry"));
		t.getAll();
		System.out.println(t.size());
		System.out.println(t.suggest("b").length);
		t.add("ra");
		t.add("aw");
		System.out.println(t.censor("rawr"));
		t.add("rawr");
		System.out.println(t.censor("rawr"));
	}//*/

	private Node root;
	private int size;
	private int maxDepth; // Not exact, but bounding for the maximum
	private boolean ignoreCase;
}
