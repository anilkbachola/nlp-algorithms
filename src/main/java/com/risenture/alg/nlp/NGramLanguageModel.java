package com.risenture.alg.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class NGramLanguageModel implements Serializable {

  private static final long serialVersionUID = 1L;
  private final int ngram;
  private final WordSeqCounterTrie wordSeqCounterTrie;
  private final double lambdaFactor;
  private int seqLength;
  private double uniformEstimate;

  /**
   * initializes an N-gram language model.
   * Uses a Trie data structure to keep the corpus of this language model.
   * @param ngram N in a N-gram model
   */
  public NGramLanguageModel(int ngram) {
    super();
    seqLength = 15;
    this.ngram = ngram;
    this.lambdaFactor = ngram;
    this.uniformEstimate = 1.0 / (double)seqLength;
    wordSeqCounterTrie = new WordSeqCounterTrie(ngram);
  }

  public int getSeqLength() {
    return seqLength;
  }

  public void setSeqLength(int seqLength) {
    this.seqLength = seqLength;
    this.uniformEstimate = 1.0 / (double)seqLength;
  }

  public double getUniformEstimate() {
    return uniformEstimate;
  }

  public void setUniformEstimate(double uniformEstimate) {
    this.uniformEstimate = uniformEstimate;
  }

  public int getNgram() {
    return ngram;
  }

  public double getLambdaFactor() {
    return lambdaFactor;
  }

  public WordSeqCounterTrie getWordSeqCounterTrie() {
    return wordSeqCounterTrie;
  }

  public void train(String statement) {
    train(statement, 1);
  }

  public void train(String statement, int incr) {
    String[] wordSeq = statement.split(" ");
    train(wordSeq, incr);
  }

  public void train(String[] wordSeq) {
    train(wordSeq, 1);
  }

  public void train(String[] wordSeq, int incr) {
    wordSeqCounterTrie.add(wordSeq,incr);
  }

  /**
   * Write the model to an output stream.
   * @param out an output stream to which the model to be written
   * @throws IOException throws IOException
   */
  public void writeTo(OutputStream out) throws IOException {
    //ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(out);
    oos.writeObject(this);
    oos.flush();
  }

  /**
   * Static initializer to initialize the language model from an input source.
   *
   * @param in an input stream from which language model to be initialized
   * @return an instance of {@link NGramLanguageModel}
   * @throws IOException throws IOException
   */
  public static NGramLanguageModel readFrom(InputStream in) throws IOException {
    ObjectInputStream ois = null;
    ois = new ObjectInputStream(in);
    Object obj = null;
    try {
      obj = ois.readObject();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return (NGramLanguageModel)obj;
  }

  /**
   * Return the list of most probable next words given a sequence.
   * @param wordSeq sequence of words
   * @return returns an array of possible words in the order of most probability
   */
  public String[] mostProbables(String[] wordSeq) {
    String[] followingWords = wordSeqCounterTrie.following(wordSeq);
    for (String following:followingWords) {
      System.out.println("word: " + following +", prob: "+ probability(wordSeq, following));
      System.out.println("*******************************************************");
    }
    //System.out.println(followingWords);
    return null;
  }

  /**
   * Probability of a word w coming after a sequence of words W.
   * @param wordSeq sequence for words
   * @param word word
   * @return probability of a 'word' coming after sequence of words
   */
  public double probability(String[] wordSeq, String word) {
    int newLength = wordSeq.length + 1;
    String[] newWordSeq = Arrays.copyOf(wordSeq, newLength);
    newWordSeq[newLength - 1] = word;
    return mlEsitmate(newWordSeq);
  }


  /**
   * Simple estimate for a given sequence.
   * @param wordSeq sequence of words
   * @return Simple probability estimate of a given sequence
   */
  public double simpleEstimate(String[] wordSeq) {
    long count = wordSeqCounterTrie.count(wordSeq);
    long extCount = wordSeqCounterTrie.count(wordSeq);
    return (double)extCount / (double)count;
  }

  /**
   * Maximum likelihood estimation for a given sequence.
   * @param wordSeq sequence of words
   * @return Maximum likelihood estimate of a given word sequence
   */
  public double mlEsitmate(String[] wordSeq) {
    return mlEsitmate(wordSeq,0,wordSeq.length);
  }

  /**
   * Maximum likelihood estimation for a given sequence.
   * Instead of multiplying the individual conditional probabilities,
   * which may result in numerical underflow (as probabilities are between 0 .. 1)
   * We add the log probabilities. Adding in log space is equivalent to multiplying in linear space
   * <code>p1×p2×p3×p4 = logp1+logp2+logp3+logp4</code>
   * @param wordSeq sequence of words
   * @param start start position in the sequence
   * @param end end position in the sequence
   * @return Maximum likelihood estimation
   */
  public final double mlEsitmate(String[] wordSeq, int start, int end) {
    double sum = 0.0;
    for (int i = start + 1; i <= end; ++i) {
      sum += log2CondEstimate(wordSeq,start,i);
    }
    return sum;
  }

  /**
   * log2 conditional estimate of a word sequence.
   * @param wordSeq sequence of words
   * @param start start position in the sequence
   * @param end end position in the sequence
   * @return log2 conditional estimate
   */
  public double log2CondEstimate(String[] wordSeq,int start,int end) {
    return log2(condEstimate(wordSeq, start, end));
  }

  /**
   * Conditional estimate of a word sequence.
   * @param wordSeq sequence of words
   * @param start start position in the sequence.
   * @param end end position in the sequence.
   * @return conditional estimate
   */
  public double condEstimate(String[] wordSeq,int start,int end) {
    return condEstimate(wordSeq, start, end, this.ngram, this.lambdaFactor);
  }

  /**
   * Conditional estimate of a word sequence
   * Gets the count and context counts and calculates the conditional probability
   * Uses Linear interpolation technique to smoothing the probability.
   * Linear interpolation uses the the conditional lambda which is calculated based
   *  on number of following words and a lambdaFactor.
   * @param wordSeq sequence of words
   * @param start start position in the sequence
   * @param end end position in the sequence
   * @param maxNGram max N-gram
   * @param lambdaFactor a lambda factor used in interpolation
   * @return conditional estimate
   */
  public double condEstimate(String[] wordSeq, int start, int end,
      int maxNGram, double lambdaFactor) {
    if (end <= start) {
      String msg = "conditional estimates require at least one word";
      throw new IllegalArgumentException(msg);
    }
    if (start == end) {
      return 0.0;
    }

    double condEstimate = uniformEstimate;//baseline estimate

    // use the minimum of maximum usable Ngram
    int ngram = Math.min(maxNGram, this.ngram);

    //for an N-gram we look only previous N-1 history.
    int contextStart = Math.max(start, end - ngram);
    int contextEnd = end - 1;

    for (int itrStart = contextEnd; itrStart >= contextStart; --itrStart) {
      // Get the sum of counts of all extension sequences, whose has same context prefix
      print(wordSeq, itrStart, contextEnd, end);
      long contextCount = wordSeqCounterTrie.contextCount(wordSeq,itrStart,contextEnd);
      if (contextCount == 0) {
        continue;
      }

      double contextSize = wordSeqCounterTrie.numFollowing(wordSeq,start,end);
      long count = wordSeqCounterTrie.count(wordSeq, itrStart, end);

      double lambda = lambda(contextCount,contextSize,lambdaFactor);

      condEstimate = lambda * (((double)count) / (double)contextCount)
          + (1.0 - lambda) * condEstimate;
      //condEstimate = (((double)count) / (double)contextCount) * condEstimate;
    }
    return condEstimate;
  }

  /**
   * Compute a lambda to be used in interpolation smoothing.
   * @param count context count
   * @param size context size
   * @param lambdaFactor lambda factor
   * @return smoothing factor
   */
  private double lambda(double count, double size, double lambdaFactor) {
    return count / (count + lambdaFactor * size);
  }

  /**
   * Compute the log2 of a given estimate.
   * @param estimate probability
   * @return logarithmic value.
   */
  private double log2(double estimate) {
    double naturalLogOf2 = Math.log(2.0);
    double base2Log = estimate * (1.0 / naturalLogOf2);
    return base2Log;
  }

  private void print(String[] wordSeq, int start, int contextEnd, int end) {
    for (int i = start; i < end; i++ ) {
      System.out.print(wordSeq[i] + " ");
    }
    System.out.println("]");
    //System.out.print("ext:["+wordSeq[end]+"]");
  }



   /**
   * main method.
   * @param args args
   */
  public static void main(String[] args) {
    NGramLanguageModel model = null;
    try {
      model = NGramLanguageModel.readFrom(new FileInputStream(new File("ngram.bin")));
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  /*
    model.train("I am a super hero");
    model.train("She is not stupid clever");
    model.train("She is stupid");
    model.train("She is stupid but clever");
    model.train("She is x but stupid");
    model.train("She is smart stupid but clever");
    model.train("She is x stupid but clever");
    model.train("She is y stupid but clever");
    model.train("She may be stupid but clever");
    //model.train("stupid is y stupid but clever");
    model.train("is y dont know but clever");
    model.train("is stupid but who");
    //model.train("is x but who");
  */
   // System.out.println(model.mostProbables(("She is x").split(" ")));
    //FileOutputStream fos;
  /*  try {
      fos = new FileOutputStream(new File("ngram.bin"));
      model.writeTo(fos);
    } catch (IOException e) {
      e.printStackTrace();
    }
  */
  }
}
