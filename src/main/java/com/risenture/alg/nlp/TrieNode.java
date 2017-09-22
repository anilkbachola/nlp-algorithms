package com.risenture.alg.nlp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TrieNode implements Serializable {
  
  private static final long serialVersionUID = 1L;

  Map<String, TrieNode> children;
  long count;

  public TrieNode() {
    super();
    children = new HashMap<>();
  }

  public TrieNode(boolean isLeaf) {
    super();
    children = new HashMap<>();
  }

  public TrieNode(Map<String, TrieNode> children, boolean isLeaf) {
    super();
    this.children = children;
  }

  boolean isLeafNode() {
    return children == null || children.size() == 0;
  }

  public Map<String, TrieNode> getChildren() {
    return children;
  }

  public void setChildren(Map<String, TrieNode> children) {
    this.children = children;
  }

  public long count() {
    return this.count;
  }

  public void setCount(long count) {
    this.count = count;
  }

  /**
   * Return the context count at this node.
   * Context count is the sum of counts of all nodes who share the same parent
   * i.e. sum of counts of all child nodes of this node
   * @return context count
   */
  public long contextCount() {
    long contextCount = 0L;
    for (Map.Entry<String, TrieNode> entry: children.entrySet()) {
      contextCount += entry.getValue().count;
    }
    return contextCount;
  }
}
