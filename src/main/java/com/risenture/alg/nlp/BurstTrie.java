package com.risenture.alg.nlp;

public class BurstTrie {

  BurstTrieNode root = new BurstTrieNode();

  /**
   * insert into bucket.
   * @param words words
   */
  public void insert(String[] words) {
    for (String word: words) {
      Integer bucketPos = Character.getNumericValue(word.charAt(0));
      if (root.bucket.containsKey(bucketPos)) {
        TrieNode node = root.bucket.get(bucketPos);
      }
    }
  }

}
