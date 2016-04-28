package edu.stanford.cs276;

import edu.stanford.cs276.util.*;

import java.io.Serializable;
import java.util.ArrayList;

public interface EditCostModel extends Serializable {
	/**
	 * This method calculates the P(R|Q) given the edit distance. Depending on the channel model
	 * you implement (uniform or empirical), the details of this method will differ
	 * @param original
	 * @param R
	 * @param distance
	 * @return
	 */
  public double editProbability(ArrayList<String> original, PossibleQuery R, int distance) throws Exception;
}
