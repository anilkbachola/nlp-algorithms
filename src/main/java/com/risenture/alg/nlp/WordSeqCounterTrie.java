package com.risenture.alg.nlp;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class WordSeqCounterTrie implements Serializable {

  private static final long serialVersionUID = 719999367852808460L;

  //TrieNode rootNode =  TrieNodeFactory.createRootNode();
  // To have separate root for each statement starting with different alphabet letter
  Map<Integer,TrieNode> rootContainer = new TreeMap<>();

  final int maxLength;

  /**
   * Creates a Trie structure which stores the counts of words in sequence.
   * This Trie will use separate containers for each starting letter of sequence
   * @param maxLength length of the words
   */
  public WordSeqCounterTrie(int maxLength) {
    this.maxLength = maxLength;
    rootContainer.put(Character.getNumericValue(' '), TrieNodeFactory.createRootNode());
  }

  private TrieNode root(char cr, boolean create) {
    Integer charUnicode = Character.getNumericValue(cr);
    TrieNode root = null;
    if (rootContainer.containsKey(charUnicode)) {
      return rootContainer.get(charUnicode);
    }

    if (create) {
      root = TrieNodeFactory.createRootNode();
      rootContainer.put(charUnicode, root);
    }
    return root;
  }

  /**
   * Add a node to the Trie.
   * Navigate to the sequence of words, increment the count by specified if already exists
   * Add a new node with the count specified.
   * @param wordSeq sequence of words (nodes) to navigate
   * @param incr increment count by
   */
  public void add(String[] wordSeq, int incr) {
    TrieNode root = root(wordSeq[0].charAt(0), true);
    Map<String, TrieNode> children = root.getChildren();
    TrieNode node = null;
    for (String word: wordSeq) {
      if (children == null || children.size() == 0) {
        node = TrieNodeFactory.createNode();
        node.setCount(incr);
        children.put(word, node);
      } else {
        if (children.containsKey(word)) {
          node = children.get(word);
          node.setCount(node.count() + incr);
        } else {
          node = TrieNodeFactory.createNode();
          node.setCount(incr);
          children.put(word, node);
        }
      }
      children =  node.getChildren();
    }
  }

  /**
   * Identify the Trie root node and navigate till end of the word sequence
   * and return the node.
   * @param wordSeq sequence of words to navigate in the Trie
   * @return trie node if found or null otherwise
   */
  public TrieNode navigate(String[] wordSeq) {
    TrieNode node = null;
    if (wordSeq.length > 0) {
      TrieNode root = root(wordSeq[0].charAt(0), false);
      if (root != null) {
        Map<String, TrieNode> children = root.getChildren();
        for (String word: wordSeq) {
          if (children != null && children.size() != 0) {
            if (children.containsKey(word)) {
              node = children.get(word);
              children = node.getChildren();
            } else {
              node = null;
            }
          }
        }
      }
    }
    return node;
  }

  /**
   * Navigate till end of the sequence from the root and return the count on the node.
   * @param wordSeq sequence of words to navigate in the Trie
   * @return count on the Trie node
   */
  public long count(String[] wordSeq) {
    TrieNode node = navigate(wordSeq);
    if (node != null) {
      return node.count();
    }

    return 0;
  }

  /**
   * Navigate till the position from the root and return the count on the node.
   * @param wordSeq sequence of words
   * @param start start position in the sequence
   * @param end end position in the sequence
   * @return return count on the Trie node
   */
  public long count(String[] wordSeq, int start, int end) {
    checkArgsStartEnd(wordSeq, start,end);
    TrieNode node = navigate(Arrays.copyOfRange(wordSeq, start, end));
    if (node != null) {
      return node.count();
    }

    return 0;
  }

  /**
   * Sum of counts of all nodes who share the same word sequence prefix.
   * @param wordSeq sequence of words
   * @param start start position in the sequence
   * @param end end position in the sequence
   * @return context count of the Trie node
   */
  public long contextCount(String[] wordSeq, int start, int end) {
    checkArgsStartEnd(wordSeq, start,end);
    String[] contextSeq = null;
    if (start == end) {
      contextSeq = new String[]{wordSeq[start]};
    } else {
      contextSeq = Arrays.copyOfRange(wordSeq, start, end);
    }
    //String[] contextSeq = start == end ?{wordSeq[start]}:Arrays.copyOfRange(wordSeq, start, end));
    TrieNode node = navigate(contextSeq);
    if (node != null) {
      return node.contextCount();
    }
    return 0;
  }

  /**
   * Navigate till end of the word sequence and return all the following words i.e. children
   * @param wordSeq sequence of words
   * @return following words of a sequence
   */
  public String[] following(String[] wordSeq) {
    TrieNode node = navigate(wordSeq);

    Map<String, TrieNode> children = node.getChildren();

    Set<String> keys = children.keySet();
    return keys.toArray(new String[keys.size()]);
  }

  /**
   * Navigate till end of the word sequence and return number of following words.
   * @param wordSeq sequence of words
   * @return number of words following a sequence
   */
  public long numFollowing(String[] wordSeq) {
    return numFollowing(wordSeq, 0, wordSeq.length);
  }

  /**
   * Navigate till end of the word sequence and return number of following words.
   * @param wordSeq sequence of words
   * @param start start position in the sequence
   * @param end end position in the sequence
   * @return number of following words of the sequence
   */
  public long numFollowing(String[] wordSeq, int start, int end) {
    checkArgsStartEnd(wordSeq, start,end);

    String[] contextSeq = null;
    if (start == end) {
      contextSeq = new String[]{wordSeq[start]};
    } else {
      contextSeq = Arrays.copyOfRange(wordSeq, start, end);
    }
    TrieNode node = navigate(contextSeq);
    if (node != null && node.children != null) {
      return node.children.size();
    }

    return 0;
  }

  private void checkArgsStartEnd(String[] wordSeq, int start, int end) {
    if (end < start) {
      String msg = "End must be >= start."
          + " Found start=" + start
          + " end=" + end;
      throw new IndexOutOfBoundsException(msg);
    }
    if (start >= 0 && end <= wordSeq.length) {
      return; // faster check
    }
    if (start < 0 || start >= wordSeq.length) {
      String msg = "Start must be greater than 0 and less than length of array."
          + " Found start=" + start
          + " Array length=" + wordSeq.length;
      throw new IndexOutOfBoundsException(msg);

    }
    if (end < 0 || end > wordSeq.length) {
      String msg = "End must be between 0 and  the length of the array."
          + " Found end=" + end
          + " Array length=" + wordSeq.length;
      throw new IndexOutOfBoundsException(msg);
    }
  }

  public static void main(String[] args){
    WordSeqCounterTrie seqCounter = new WordSeqCounterTrie(6);
    String[] words = {"I","am","a","super","hero"};
    seqCounter.add(words, 1);
    String[] words1 = {"I","am","not","stupid"};
    seqCounter.add(words1, 1);

    String[] words2 = {"He","is","not","stupid"};
    seqCounter.add(words2, 1);

    String[] words3 = {"She","is","not","stupid"};
    seqCounter.add(words3, 1);

    String[] words4 = {"She","is","stupid"};
    seqCounter.add(words4, 1);

  }
}
