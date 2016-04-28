package edu.stanford.cs276;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CandidateGenerator implements Serializable {

	private static final long serialVersionUID = 1L;
	private static CandidateGenerator cg_;
	private static final int MIN_RELEVANCE_THRESHOLD = 3;

	// Don't use the constructor since this is a Singleton instance
	private CandidateGenerator() {
	}

	public static CandidateGenerator get() throws Exception {
		if (cg_ == null) {
			cg_ = new CandidateGenerator();
		}
		return cg_;
	}

	public static final Character[] alphabet = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', ' ', ',' };
	
	public static final Character[] noNumbers = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' ', ',' };

	// Generate all candidates for the target query
	public Set<ArrayList<String>> getCandidates(String query) throws Exception {
		LanguageModel lm = LanguageModel.load();
		Character[] alphabetToUse = null;
		Set<ArrayList<String> > candidates = new HashSet<ArrayList<String> >();
		Set<ArrayList<String> > prevCands = null;
		String[] queryWords = query.trim().split(" ");
		String prevQWord = null;
		for (String word : queryWords) {
			
			//If there are no digits in the word, it is unlikely that we should add any.
			if (word.matches(".*\\d+.*")) {
				alphabetToUse = alphabet;
			} else {
				alphabetToUse = noNumbers;
			}
			Set<String> wordCands = generateCandidateWords(word, lm, alphabetToUse);
			if (lm.wordExists(word)) {
				wordCands.add(word);
			}

			System.out.println("Word: " + word + ", num candidates: " + wordCands.size());
			// Perform Cartesian Product
			if (prevCands != null) {
				
				Set<ArrayList<String> > currSet = new HashSet<ArrayList<String>>();
				for (String candidate : wordCands) {

					for (ArrayList<String> candQuery: prevCands) {
						ArrayList<String> newCandQuery = new ArrayList<String>(candQuery);
						
						//in the case where we combine words
						if (candQuery.get(candQuery.size() - 1).equals(prevQWord) && candidate.equals(word)) { 
							String combined = prevQWord + word;
							newCandQuery.set(candQuery.size() - 1, combined);
						} else {
							String[] words = candidate.split(" ");
							if (words.length > 1) {
								System.out.println("Adding words: " + words[0] + " and " + words[1]);
								newCandQuery.add(words[0]);
								newCandQuery.add(words[1]);
							} else {
								newCandQuery.add(candidate);
							}
						}
						currSet.add(newCandQuery);
					}
				}
				prevCands = currSet;
			} else {
				prevCands = new HashSet<ArrayList<String> >();
				for (String candidate: wordCands) {
					ArrayList<String> newQuery = new ArrayList<String>();
					newQuery.add(candidate);
					prevCands.add(newQuery);
				}
			}
			System.out.println("set size for candidates: " + prevCands.size());
			prevQWord = word;
		}
		candidates = prevCands;
		return candidates;
	}

	private Set<String> generateCandidateWords(String word, LanguageModel lm, Character[] alphabetToUse) {
		Set<String> candidates = new HashSet<String>();
		int wordLen = word.length();
		System.out.println("Word: " + word);

		//Deletions
		for (int i = 0; i < wordLen; i++) {
			String deleteWord = null;
			if (i != wordLen - 1) {
				deleteWord = word.substring(0, i) + word.substring(i + 1); // delete
			} else {
				deleteWord = word.substring(0, i);
			}
			if (lm.unigram.count(deleteWord) > MIN_RELEVANCE_THRESHOLD) {
				candidates.add(deleteWord);
			}
			
		}

		//Insertions and replacements in the alphabet
		for (int i = 0; i < wordLen; i++) {
			for (Character c : alphabetToUse) {
				String insertWord = "";
				String replaceWord = "";
				insertWord = word.substring(0, i + 1) + c + word.substring(i + 1); 
				replaceWord = word.substring(0, i) + c + word.substring(i + 1);
				if (c.equals(' ')) {
					String[] words = insertWord.split(" ");
					if (words.length > 1 && lm.unigram.count(words[0]) > MIN_RELEVANCE_THRESHOLD
							&& lm.unigram.count(words[1]) > MIN_RELEVANCE_THRESHOLD) {
						System.out.println(word);
						candidates.add(word);
					}
				} else if (lm.unigram.count(insertWord) > MIN_RELEVANCE_THRESHOLD) {
					candidates.add(insertWord);
				}				
				if (lm.unigram.count(replaceWord) > MIN_RELEVANCE_THRESHOLD) {
					candidates.add(replaceWord);
				}
			}
		}
		
		//Transpositions
		for (int i = 0; i < wordLen - 1; i++) {
			String transposeWord = word.substring(0, i) + word.charAt(i + 1) + word.charAt(i)
					+ word.substring(i + 2);
			if (lm.unigram.count(transposeWord) > MIN_RELEVANCE_THRESHOLD) {
				candidates.add(transposeWord);
			}
		}

		return candidates;
	}

}
