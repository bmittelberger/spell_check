package edu.stanford.cs276;

import java.io.BufferedReader;
import java.util.PriorityQueue;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.cs276.util.Pair;
import edu.stanford.cs276.util.PossibleQuery;

public class RunCorrector {

	public static LanguageModel languageModel;
	public static NoisyChannelModel nsm;

	private static int PRUNING_THRESHOLD = 30;

	public static void main(String[] args) throws Exception {
    System.out.println("Starting Corrector...");
    // Parse input arguments
    String uniformOrEmpirical = null;
    String queryFilePath = null;
    String goldFilePath = null;
    String extra = null;
    BufferedReader goldFileReader = null;
    if (args.length == 2) {
      // Run without extra and comparing to gold
      uniformOrEmpirical = args[0];
      queryFilePath = args[1];
    } else if (args.length == 3) {
      uniformOrEmpirical = args[0];
      queryFilePath = args[1];
      if (args[2].equals("extra")) {
        extra = args[2];
      } else {
        goldFilePath = args[2];
      }
    } else if (args.length == 4) {
      uniformOrEmpirical = args[0];
      queryFilePath = args[1];
      extra = args[2];
      goldFilePath = args[3];
    } else {
      System.err.println(
          "Invalid arguments.  Argument count must be 2, 3 or 4 \n"
          + "./runcorrector <uniform | empirical> <query file> \n"
          + "./runcorrector <uniform | empirical> <query file> <gold file> \n"
          + "./runcorrector <uniform | empirical> <query file> <extra> \n"
          + "./runcorrector <uniform | empirical> <query file> <extra> <gold file> \n"
          + "SAMPLE: ./runcorrector empirical data/queries.txt \n"
          + "SAMPLE: ./runcorrector empirical data/queries.txt data/gold.txt \n"
          + "SAMPLE: ./runcorrector empirical data/queries.txt extra \n"
          + "SAMPLE: ./runcorrector empirical data/queries.txt extra data/gold.txt \n");
      return;
    }

    if (goldFilePath != null) {
      goldFileReader = new BufferedReader(new FileReader(new File(goldFilePath)));
    }

    // Load models from disk
    System.out.println("Loading Language Model...");
    languageModel = LanguageModel.load();
    System.out.println("Done Loading Language Model");
    nsm = NoisyChannelModel.load();
    BufferedReader queriesFileReader = new BufferedReader(new FileReader(new File(queryFilePath)));
    nsm.setProbabilityType(uniformOrEmpirical);

    String query = null;

    /*
     * Each line in the file represents one query. We loop over each query and find
     * the most likely correction
     */
    
    double numCorrect = 0;
    double totalQueries = 0;
    
    while ((query = queriesFileReader.readLine()) != null) {

    
      String correctedQuery = query;
      String[] q = query.split("\\s+");
      ArrayList<String> origQuery = new ArrayList<String>();
      Set< PossibleQuery > possibleQueries = null;
      String prevWord = null;
      
      EditCostModel scorer = null;
      if (uniformOrEmpirical.equals("uniform")) {
    	  scorer = new UniformCostModel();
      }
//      } else {
//    	  scorer = new EmpiricalCostModel();
//      }

      
      for (String w : q) {
    	 
    	  origQuery.add(w);
    	  possibleQueries = CandidateGenerator.get().getCandidates(possibleQueries, w, prevWord);
    	  PriorityQueue<PossibleQuery> pruned = new PriorityQueue<PossibleQuery>();
    	  for (PossibleQuery pq : possibleQueries) {
    		  pq.setScore(scorer.editProbability(origQuery, pq, 2));
    		  pruned.add(pq);
    	  }
    	  
    	  PossibleQuery pq = null;
    	  Set<PossibleQuery> prunedQueries = new HashSet<PossibleQuery>();
    	  while ((pq = pruned.poll()) != null && prunedQueries.size() <= PRUNING_THRESHOLD) {
//    		  System.out.println(pq.toString());
    		  prunedQueries.add(pq);
    	  }
    	  possibleQueries = prunedQueries;
    	  prevWord = w;
      }
      
      double max = 0;
      PossibleQuery best = new PossibleQuery(new Pair<String,String> ("",""));
      for (PossibleQuery pq : possibleQueries) {
  		if (pq.getScore() > max) {
  			best = pq;
  			max = pq.getScore();
  		}
      }
      
      
      
      if ("extra".equals(extra)) {
        /*
         * If you are going to implement something regarding to running the corrector,
         * you can add code here. Feel free to move this code block to wherever
         * you think is appropriate. But make sure if you add "extra" parameter,
         * it will run code for your extra credit and it will run you basic
         * implementations without the "extra" parameter.
         */
      }

      // If a gold file was provided, compare our correction to the gold correction
      // and output the running accuracy
      if (goldFileReader != null) {
    	
        String goldQuery = goldFileReader.readLine();
        
        int diff = queryDiff(goldQuery, best.asQuery());
        if (diff > 0) {
        	System.out.println("MISMATCH:");
        	System.out.println("ORIG: \"" + query + "\"");
        	System.out.println("GOLD: \"" + goldQuery);
        	System.out.println("YOUR: \"" + best.asQuery() + "\"");
        	System.out.println();
        	numCorrect--;
        }
//        int queryLen = goldQuery.split(" ").length;
        totalQueries ++;
        numCorrect ++;
//        
        
        
        /*
         * You can do any bookkeeping you wish here - track accuracy, track where your solution
         * diverges from the gold file, what type of errors are more common etc. This might
         * help you improve your candidate generation/scoring steps 
         */
      }
      
      /*
       * Output the corrected query.
       * IMPORTANT: In your final submission DO NOT add any additional print statements as 
       * this will interfere with the autograder
       */
    }
    
    if (goldFilePath != null ){
	    System.out.println("STATISTICS: ");
	    System.out.println("TOTAL WORDS: " + Double.toString(totalQueries));
	    System.out.println("NUM CORRECT: " + numCorrect);
	    System.out.println("PCT CORRECT: " + Double.toString(numCorrect / totalQueries * 100) + "%" );
    }
    
//    System.out.println("the are: " + languageModel.bigram.count("the,are"));
    queriesFileReader.close();
  }

	public static int queryDiff(String gold, String bestQuery) {
		String[] goldWords = gold.split(" ");
		String[] bestWords = bestQuery.split(" ");
		int differences = 0;
		for (int i = 0; i < goldWords.length && i < bestWords.length; i++) {
			if (!goldWords[i].equals(bestWords[i])) {
				differences++;
			}
		}
		return differences;
	}
}
