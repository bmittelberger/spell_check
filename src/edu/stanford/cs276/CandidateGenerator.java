package edu.stanford.cs276;

import java.io.Serializable;
import edu.stanford.cs276.util.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.cs276.util.Pair;

public class CandidateGenerator implements Serializable {

	private static final long serialVersionUID = 1L;
	private static CandidateGenerator cg_;
	private static final int MIN_RELEVANCE_THRESHOLD = 10;

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
			'9', ' ', ',', '\'' };
	
	public static final Character[] noNumbers = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' ', ',', '\''};

	// Generate all candidates for the target query
	public Set<PossibleQuery > getCandidates(Set<PossibleQuery > prevQueries, 
				String word, String prevWord) throws Exception {
		LanguageModel lm = LanguageModel.load();
		Character[] alphabetToUse = null;
		Set<PossibleQuery> candidates = new HashSet<PossibleQuery>();
		
		
		//prevWord are no digits in the word, it is unlikely that we should add any.
		if (word.matches(".*\\d+.*")) {
			alphabetToUse = alphabet;
		} else {
			alphabetToUse = noNumbers;
		}
		
		Set<Pair<String, String> > wordCands = null;
		if (!word.matches(".*[a-zA-Z].*")) {
			wordCands = new HashSet<Pair < String, String > > ();
		} else {
			wordCands = generateCandidateWords(word, lm, alphabetToUse);
		}
		 
		if (lm.wordExists(word)) {
			wordCands.add(new Pair<String, String>(word, "none"));
		}

		
		
		
		
		// Perform Cartesian Product
		if (prevQueries != null) {
			
			//Check to see if we want to combine words
			String combined = prevWord + word;
			Set<PossibleQuery > currSet = new HashSet<PossibleQuery >();
			if (lm.unigram.count(combined) > MIN_RELEVANCE_THRESHOLD) {
				for (PossibleQuery pq : prevQueries) {
					ArrayList<Pair<String, String> > candQuery = pq.getQuery();
					ArrayList<Pair<String, String> > newCandQuery = new ArrayList<Pair <String, String> >(candQuery);
					String edit = "del-" + prevWord.charAt(prevWord.length() - 1) + "- ";
					newCandQuery.set(candQuery.size() - 1, new Pair<String, String>(combined, edit));
					currSet.add(new PossibleQuery(newCandQuery));
				}
			}
			
			//Find the rest of the cartesian products
			for (Pair<String, String> candidate : wordCands) {
				for (PossibleQuery candQueryObj: prevQueries) {
					ArrayList<Pair<String, String> > candQuery = candQueryObj.getQuery();
					ArrayList<Pair<String, String> > newCandQuery = new ArrayList<Pair <String, String> >(candQuery);
					newCandQuery.add(candidate);
					currSet.add(new PossibleQuery(newCandQuery));
				}
			}
			prevQueries = currSet;
		} else {
			prevQueries = new HashSet<PossibleQuery >();
			for (Pair<String, String> candidate: wordCands) {
				PossibleQuery newQuery = new PossibleQuery(candidate);
				prevQueries.add(newQuery);
			}
		}
		candidates = prevQueries;
		return candidates;
	}

	private Set<Pair<String, String> > generateCandidateWords(String word, LanguageModel lm, Character[] alphabetToUse) {
		Set<Pair<String, String>> candidates = new HashSet<Pair<String, String>>();
		int wordLen = word.length();

		//Deletions
		for (int i = 0; i < wordLen; i++) {
			String deleteWord = null;
			if (i != wordLen - 1) {
				deleteWord = word.substring(0, i) + word.substring(i + 1); // delete
			} else {
				deleteWord = word.substring(0, i);
			}
			
			if (lm.unigram.count(deleteWord) > MIN_RELEVANCE_THRESHOLD) {
				String edit = this.createEdit("del", word, i, word.charAt(i));
				Pair<String, String> deletePair = new Pair<String, String>(deleteWord, edit);
				candidates.add(deletePair);
			}
			
		}

		//Insertions and replacements in the alphabet
		for (int i = 0; i < wordLen; i++) {
			for (Character c : alphabetToUse) {
				String insertWord = "";
				String replaceWord = "";
				if (i == wordLen - 1) {
					insertWord = word + c;
				} else {
					insertWord = word.substring(0, i) + c + word.substring(i); 
				}
				

				if (c.equals(' ')) {
					String[] words = insertWord.split(" ");
					if (words.length > 1 && lm.unigram.count(words[0]) > MIN_RELEVANCE_THRESHOLD
							&& lm.unigram.count(words[1]) > MIN_RELEVANCE_THRESHOLD) {
						String edit = this.createEdit("ins", word, i + 1, c);
						candidates.add(new Pair<String, String>(insertWord, edit));
					}
				} else if (lm.unigram.count(insertWord) > MIN_RELEVANCE_THRESHOLD) {
					String edit = this.createEdit("ins", word, i + 1, c);
					candidates.add(new Pair<String, String>(insertWord, edit));
				}		
				if (c != word.charAt(i)) {
					replaceWord = word.substring(0, i) + c + word.substring(i + 1);
					if (lm.unigram.count(replaceWord) > MIN_RELEVANCE_THRESHOLD) {
						String edit = "rep-";
						edit += word.charAt(i);
						edit += "-" + c;
						candidates.add(new Pair<String, String>(replaceWord, edit));
					}
				}
			}
		}
		
		//Transpositions
		for (int i = 0; i < wordLen - 1; i++) {
			String transposeWord = word.substring(0, i) + word.charAt(i + 1) + word.charAt(i)
					+ word.substring(i + 2);
			if (lm.unigram.count(transposeWord) > MIN_RELEVANCE_THRESHOLD) {
				String edit = this.createEdit("tra", word, i+1, word.charAt(i+1));
				candidates.add(new Pair<String, String>(transposeWord, edit));
			}
		}

		return candidates;
	}
	
	String createEdit(String type, String word, int i, Character editChar) {
		String edit = type + "-";
		if (type.equals("del") || type.equals("tra")) {
			if (i == 0) {
				edit += "BEGIN$";
			} else {
				edit += word.charAt(i - 1);
			}
			edit += "-" + editChar;
		} else {
			if (i - 1 == 0) {
				edit += "BEGIN$";
			} else {
				edit += word.charAt(i - 2);
			}
			edit += "-" + editChar;
		}
		return edit;
	}

}
