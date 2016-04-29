package edu.stanford.cs276;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import edu.stanford.cs276.util.Pair;
import edu.stanford.cs276.util.PossibleQuery;

/**
 * Implement {@link EditCostModel} interface. Use the query corpus to learn a model
 * of errors that occur in our dataset of queries, and use this to compute P(R|Q)
 */
public class EmpiricalCostModel implements EditCostModel {
	private static final long serialVersionUID = 1L;
	private static double lambda = .5;
	//private static double mu = 1.0; // the dampening factor on language model p(Q)
	Map<String, Integer> uni_letter_counts = new HashMap<String, Integer>();
	Map<Pair<String, String>, Integer> bi_letter_counts = new HashMap<Pair<String, String>, Integer>();
	Map<Pair<String, String>, Integer> sub_counts = new HashMap<Pair<String, String>, Integer>();
	Map<Pair<String, String>, Integer> insert_counts = new HashMap<Pair<String, String>, Integer>();
	Map<Pair<String, String>, Integer> delete_counts = new HashMap<Pair<String, String>, Integer>();
	Map<Pair<String, String>, Integer> transp_counts = new HashMap<Pair<String, String>, Integer>();
	

		public EmpiricalCostModel(String editsFile) throws IOException {
			BufferedReader input = new BufferedReader(new FileReader(editsFile));
			System.out.println("Constructing edit distance map...");


					
			uni_letter_counts.put("BEGIN$", Integer.valueOf(0));

			String line = null;
			while ((line = input.readLine()) != null) {
				Scanner lineSc = new Scanner(line);
				lineSc.useDelimiter("\t");

				String noisy = lineSc.next();
				String clean = lineSc.next();

				// System.out.println( noisy );
				// System.out.println( clean );
				Boolean editFound = false;

				if (noisy.length() > clean.length()) {

					// System.out.println( "In insert..." );
					uni_letter_counts.put("BEGIN$", uni_letter_counts.get("BEGIN$") + 1) ;
					Pair<String, String> start = new Pair<String, String>("BEGIN$", String.valueOf(clean.charAt(0)));
					if (bi_letter_counts.containsKey(start)) {
						bi_letter_counts.put(start, bi_letter_counts.get(start) + 1);
					} else {
						bi_letter_counts.put(start, Integer.valueOf(1));
					}

					for (int i = 0; i < noisy.length() - 1; i++) {

						// update unigram letter counts

						if (uni_letter_counts.containsKey( String.valueOf(clean.charAt(i)) ) ) {
							//Integer current = uni_letter_counts.get(String.valueOf(clean.charAt(i)));
							uni_letter_counts.put( String.valueOf(clean.charAt(i) ) , uni_letter_counts.get( String.valueOf( clean.charAt( i ) ) ) + 1 );
						} else {
							uni_letter_counts.put(String.valueOf(clean.charAt(i)), Integer.valueOf(1));
						}

						// update bigram letter counts for transpose calculation
						if (i != clean.length() - 1) {
							Pair<String, String> true_pair = new Pair<String, String>(String.valueOf(clean.charAt(i)),
									String.valueOf(clean.charAt(i + 1)));
							if (bi_letter_counts.containsKey(true_pair)) {
								//Integer current = bi_letter_counts.get(true_pair);
								bi_letter_counts.put(true_pair, bi_letter_counts.get( true_pair ) + 1 );
							} else {
								bi_letter_counts.put(true_pair, Integer.valueOf(1));
							}
						}

						if (!editFound) {
							if (clean.charAt(i) != noisy.charAt(i)) {
								editFound = true;
								if (i != 0) {
									Pair<String, String> insert_pair = new Pair<String, String>(
											String.valueOf(noisy.charAt(i - 1)), String.valueOf(noisy.charAt(i)));
									// System.out.println( "found insert" );
									// System.out.println( insert_pair );
									if (insert_counts.containsKey(insert_pair)) {
										insert_counts.put(insert_pair, insert_counts.get(insert_pair) + 1);
									} else {
										insert_counts.put(insert_pair, Integer.valueOf(1));
									}
								} else {
									Pair<String, String> insert_pair = new Pair<String, String>("BEGIN$",
											String.valueOf(noisy.charAt(i)));
									// System.out.println( "found insert" );
									// System.out.println( insert_pair );
									if (insert_counts.containsKey(insert_pair)) {
										insert_counts.put(insert_pair, insert_counts.get(insert_pair) + 1);
									} else {
										insert_counts.put(insert_pair, Integer.valueOf(1));
									}
								}

							}
						}
					}

					// final condition , if you haven't found the edit having
					// iterated from 0 , noisy.length - 1 , then the insert is in
					// the last position
					if (!editFound) {
						Pair<String, String> insert_pair = new Pair<String, String>(
								String.valueOf(noisy.charAt(noisy.length() - 2)),
								String.valueOf(noisy.charAt(noisy.length() - 1)));
						// System.out.println( "found insert" );
						// System.out.println( insert_pair );
						if (insert_counts.containsKey(insert_pair)) {
							insert_counts.put(insert_pair, insert_counts.get(insert_pair) + 1);
						} else {
							insert_counts.put(insert_pair, Integer.valueOf(1));
						}

					}
				}

				else if (noisy.length() < clean.length()) {
					// System.out.println( "In delete..." );
					// something has been deleted, logic should be similar to above
					// but we iterate through clean instead of noisy
					uni_letter_counts.put("BEGIN$", uni_letter_counts.get("BEGIN$") + 1);

					Pair<String, String> start = new Pair<String, String>(String.valueOf("BEGIN$"),
							String.valueOf(clean.charAt(0)));
					if (bi_letter_counts.containsKey(start)) {
						bi_letter_counts.put(start, bi_letter_counts.get(start) + 1);
					} else {
						bi_letter_counts.put(start, Integer.valueOf(1));
					}

					for (int i = 0; i < clean.length() - 1; i++) {

						// update unigram letter counts
						if (uni_letter_counts.containsKey(String.valueOf(clean.charAt(i)))) {
							//Integer current = uni_letter_counts.get(String.valueOf(clean.charAt(i)));
							uni_letter_counts.put(String.valueOf(clean.charAt(i)), uni_letter_counts.get( String.valueOf(clean.charAt(i)) ) + 1 );
						} else {
							uni_letter_counts.put(String.valueOf(clean.charAt(i)), Integer.valueOf(1));
						}

						// update bigram letter counts
						Pair<String, String> true_pair = new Pair<String, String>(String.valueOf(clean.charAt(i)),
								String.valueOf(clean.charAt(i + 1)));
						if (bi_letter_counts.containsKey(true_pair)) {
							// Integer current = bi_letter_counts.get(true_pair);
							bi_letter_counts.put(true_pair, bi_letter_counts.get( true_pair ) + 1 );
						} else {
							bi_letter_counts.put(true_pair, Integer.valueOf(1));
						}

						if (!editFound) {
							if (clean.charAt(i) != noisy.charAt(i)) {
								editFound = true;

								if (i != 0) {
									Pair<String, String> delete_pair = new Pair<String, String>(
											String.valueOf(clean.charAt(i - 1)), String.valueOf(clean.charAt(i)));
									// System.out.println( "found delete" );
									// System.out.println( delete_pair );
									if (delete_counts.containsKey(delete_pair)) {
										delete_counts.put(delete_pair, delete_counts.get(delete_pair) + 1);
									} else {
										delete_counts.put(delete_pair, Integer.valueOf(1));
									}
								} else {
									Pair<String, String> delete_pair = new Pair<String, String>("BEGIN$",
											String.valueOf(clean.charAt(i)));
									// System.out.println( "found delete" );
									// System.out.println( delete_pair );
									if (delete_counts.containsKey(delete_pair)) {
										delete_counts.put(delete_pair, delete_counts.get(delete_pair) + 1);
									} else {
										delete_counts.put(delete_pair, Integer.valueOf(1));
									}
								}
							}
						}
					}
					if (!editFound) {
						Pair<String, String> delete_pair = new Pair<String, String>(
								String.valueOf(clean.charAt(clean.length() - 2)),
								String.valueOf(clean.charAt(clean.length() - 1)));
						// System.out.println( "found delete" );
						// System.out.println( delete_pair );
						if (delete_counts.containsKey(delete_pair)) {
							delete_counts.put(delete_pair, delete_counts.get(delete_pair) + 1);
						} else {
							delete_counts.put(delete_pair, Integer.valueOf(1));
						}
					}

				}

				else {
					// System.out.println( "In transpose/substitution..." );
					uni_letter_counts.put("BEGIN$", uni_letter_counts.get("BEGIN$") + 1);
					Pair<String, String> start = new Pair<String, String>("BEGIN$", String.valueOf(clean.charAt(0)));
					if (bi_letter_counts.containsKey(start)) {
						bi_letter_counts.put(start, bi_letter_counts.get(start) + 1);
					} else {
						bi_letter_counts.put(start, Integer.valueOf(1));
					}

					for (int i = 0; i < clean.length() - 1; i++) {

						// update unigram letter counts
						if (uni_letter_counts.containsKey(String.valueOf(clean.charAt(i)))) {
							//Integer current = uni_letter_counts.get(String.valueOf(clean.charAt(i)));
							uni_letter_counts.put(String.valueOf(clean.charAt(i)), uni_letter_counts.get(String.valueOf(clean.charAt(i))) + 1 );
						} else {
							uni_letter_counts.put(String.valueOf(clean.charAt(i)), Integer.valueOf(1));
						}

						// update bigram letter counts
						Pair<String, String> true_pair = new Pair<String, String>(String.valueOf(clean.charAt(i)),
								String.valueOf(clean.charAt(i + 1)));
						if (bi_letter_counts.containsKey(true_pair)) {
							// Integer current = bi_letter_counts.get(true_pair);
							bi_letter_counts.put(true_pair, bi_letter_counts.get( true_pair ) + 1 );
						} else {
							bi_letter_counts.put(true_pair, Integer.valueOf(1));
						}

						if (!editFound) {
							if (clean.charAt(i) != noisy.charAt(i)) {
								editFound = true;
								if (clean.charAt(i) == noisy.charAt(i + 1) && clean.charAt(i + 1) == noisy.charAt(i)) {
									// transpose
									Pair<String, String> transp_pair = new Pair<String, String>(
											String.valueOf(clean.charAt(i)), String.valueOf(clean.charAt(i + 1)));
									// System.out.println( "found transpose" );
									// System.out.println( transp_pair );

									if (transp_counts.containsKey(transp_pair)) {
										transp_counts.put(transp_pair, transp_counts.get(transp_pair) + 1);
									} else {
										transp_counts.put(transp_pair, Integer.valueOf(1));
									}
								} else {
									Pair<String, String> sub_pair = new Pair<String, String>(
											String.valueOf(clean.charAt(i)), String.valueOf(noisy.charAt(i)));
									// System.out.println( "found substitution" );
									// System.out.println( sub_pair );

									if (sub_counts.containsKey(sub_pair)) {
										sub_counts.put(sub_pair, sub_counts.get(sub_pair) + 1);
									} else {
										sub_counts.put(sub_pair, Integer.valueOf(1));
									}
								}
							}
						}
					}
					if (!editFound) {
						Pair<String, String> sub_pair = new Pair<String, String>(
								String.valueOf(clean.charAt(clean.length() - 1)),
								String.valueOf(noisy.charAt(noisy.length() - 1)));
						// System.out.println( "found substitution" );
						// System.out.println( sub_pair );

						if (sub_counts.containsKey(sub_pair)) {
							sub_counts.put(sub_pair, sub_counts.get(sub_pair) + 1);
						} else {
							sub_counts.put(sub_pair, Integer.valueOf(1));
						}
					}
				}

			}
			input.close();
			System.out.println("Done.");
		}


  // You need to update this to calculate the proper empirical cost
  @Override
  public double editProbability(ArrayList<String> original , PossibleQuery Q, int distance ) throws Exception {
	  
	  
	 double p_r_q = 1.0; 
	 double p_q = 1.0;
	 
	 LanguageModel lm = LanguageModel.load();

	 
	 ArrayList<Pair<String, String> > word_pairs = Q.getQuery();
	 ArrayList<String> candidate_words = new ArrayList<String>();
	 
	 Boolean edit_found = false;
	  
	 for ( Pair<String,String> pair : word_pairs ){
		 String[] words = pair.getFirst().split( " " );
		 String edit = pair.getSecond() ;
		 if ( ! edit.equals( "none" ) ) {
			 edit_found = true;
			 p_r_q *= computeEditCost( edit );
		 }
		 
		 for ( int i = 0 ; i < words.length ; i ++ ){
		  candidate_words.add( words[ i ] ); 
		 }
		 
	 }
	 if ( ! edit_found ){
		 p_r_q = .95;
	 }
	// System.out.println( "P(R|Q): " + p_r_q ); 
	 
	 for ( int i = 0 ; i < candidate_words.size(); i ++  ){
	
		  if (i == 0) {
			  double prob = (double) lm.unigram.count( candidate_words.get( i ) ) / (double)lm.unigram.getTermCount() ; 
			  p_q *= prob ;
		  }
		  else {
			  double unigram_prob = (double)lm.unigram.count( candidate_words.get( i ) ) / (double)lm.unigram.getTermCount();
			  double bigram_prob = (double)lm.bigram.count( candidate_words.get( i - 1 ) + "," + candidate_words.get( i ) ) / (double)lm.unigram.count( candidate_words.get( i - 1 ) );
			  double interp = lambda*unigram_prob + ( 1.0 - lambda)*bigram_prob;
			  p_q *= interp;
		  }
	  }
	  
	 
	 return p_r_q * Math.pow( p_q , 1.5 );

  }
  
  public double computeEditCost( String edit ){
	  String[] edit_elems = edit.split( "-" );
	  String edit_type = edit_elems[ 0 ];
	  String edit_1 = edit_elems[ 1 ];
	  String edit_2 = edit_elems[ 2 ];
	  
	  
	  Pair<String,String> letter_pair = new Pair<String,String>( edit_1, edit_2 );
	  
	  double vocab_size = (double) uni_letter_counts.keySet().size();
	//  System.out.println( "VOCAB SIZE: " + vocab_size  );
	  
	  double numerator = 1.0 ;
	  double denominator = 1.0;
	  if ( edit_type.equals( "del" ) ){
		  if ( delete_counts.get( letter_pair ) != null ){ 	// perform plus one smoothing dynamically
			 numerator = ( (double) delete_counts.get( letter_pair ).intValue() ) + 1.0 ;
		  }
		  else{
			 numerator = 1.0; 
		  }
		  denominator = (double) bi_letter_counts.get( letter_pair ).intValue();
		  denominator += vocab_size;
	  }
	  else if ( edit_type.equals( "ins" ) ){
		  if ( insert_counts.get( letter_pair ) != null ){
			  numerator = ((double) insert_counts.get( letter_pair ).intValue()) + 1.0 ;
		  }
		  else{
			  numerator = 1.0;
		  }
		  denominator = (double) uni_letter_counts.get( edit_1 ).intValue();
		  denominator += vocab_size;
	  }
	  else if ( edit_type.equals(  "rep" ) ) {
		  if ( sub_counts.get( letter_pair ) != null ){
			  numerator = ((double) sub_counts.get( letter_pair ).intValue()) + 1.0 ;
		  }
		  else{
			  numerator = 1.0;
		  }
		  denominator = (double) uni_letter_counts.get( edit_1 ).intValue();
		  denominator += vocab_size;  
	  }
	  else {
		  if ( transp_counts.get( letter_pair ) != null ){
			  numerator = ((double) transp_counts.get( letter_pair ).intValue()) + 1.0;
		  }
		  else{
			  numerator = 1.0;
		  }
		  denominator = (double) bi_letter_counts.get( letter_pair ).intValue();
		  denominator += 2.0;
	  }
	  
	 // System.out.println( "EDIT: " + edit );
	  //System.out.println( "NUMERATOR: " + numerator );
	  //System.out.println( "DENOMINATOR: " + denominator );
	  //System.out.println( "PROB: " + numerator / denominator );
	  
	  return numerator / denominator ;
  }
}
