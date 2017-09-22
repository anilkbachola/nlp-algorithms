package com.risenture.alg.nlp;

public interface WordSeqCounter {

	/**
	 * Returns the count of the wordSeq
	 * @param wordSeq, sequence of words
	 * @param start, index of starting word in the sequence
	 * @param end, index of ending word in the sequence
	 * @return
	 */
	public long count(String[] wordSeq, int start, int end);
	
	/**
	 * Returns the sum of count of all word sequences of children
	 * @param wordSeq
	 * @param start
	 * @param end
	 * @return
	 */
	public long extensionCount(String[] wordSeq, int start, int end);
	
	/**
	 * Returns the array of following
	 * @param wordSeq
	 * @param start
	 * @param end
	 * @return
	 */
	public String[] following(String[] wordSeq, int start, int end);
}
