package edu.stanford.cs276;

import java.util.ArrayList;
import edu.stanford.cs276.util.*;
/**
 * Implement {@link EditCostModel} interface by assuming assuming
 * that any single edit in the Damerau-Levenshtein distance is equally likely,
 * i.e., having the same probability
 */
public class UniformCostModel implements EditCostModel {
	
	private static final long serialVersionUID = 1L;
	private static double editCost = .05; // the uniform cost associated with a single edit 
	private static double identityProbability = .95; // the probability of candidate Q | Q = R
	private static double mu = 1.0; // the dampening factor on language model p(Q)
	private static double lambda = .5; // the interpolation parameter for language model smoothing
  @Override
  public double editProbability(ArrayList<String> original, PossibleQuery Q, int distance ) throws Exception {
	 
	  double p_r_q = 1; 
	  double p_q = 1;
	  
	
	  LanguageModel lm = LanguageModel.load();

	  // compute P( R | Q ) 
	  int count_mistakes = 0;
	  for ( int i = 0 ; i < original.size() && i < Q.getQuery().size(); i ++ ){
		  if ( ! original.get( i ).equals( Q.getQuery().get( i ).getFirst() ) ) {
			  count_mistakes += 1;
		  }
	  }
	  if (count_mistakes > 0) {
		  p_r_q = Math.pow( editCost , (double) count_mistakes ); 
	  }
	  else{ 
		  p_r_q = identityProbability; 
	  }
	  
	  for ( int i = 0 ; i < Q.getQuery().size(); i ++  ){
		  if (i == 0) {
			  double prob = (double)lm.unigram.count( Q.getQuery().get( i ).getFirst() ) / (double)lm.unigram.getTermCount() ; 
			  p_q *= prob ;
		  }
		  else {
			  double unigram_prob = (double)lm.unigram.count( Q.getQuery().get( i ).getFirst() ) / (double)lm.unigram.getTermCount();
			  double bigram_prob = (double)lm.bigram.count( Q.getQuery().get( i - 1 ).getFirst() + "," + (Q.getQuery().get( i ).getFirst() )) / (double)lm.unigram.count( Q.getQuery().get(i - 1 ).getFirst());
			  double interp = lambda*unigram_prob + ( 1.0 - lambda)*bigram_prob;
			  p_q *= interp;
		  }
	  }
	  	  
	  return p_r_q * Math.pow( p_q , mu );
    
  }
 
}