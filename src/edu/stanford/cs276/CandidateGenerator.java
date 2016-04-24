package edu.stanford.cs276;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class CandidateGenerator implements Serializable {

	private static final long serialVersionUID = 1L;
	private static CandidateGenerator cg_;

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

	// Generate all candidates for the target query
	public Set<String[]> getCandidates(String query, LanguageModel lm) throws Exception {
		Set<String[]> candidates = new HashSet<String[]>();

		
		Set<String[]> prevCands = null;
		String[] queryWords = query.trim().split(" ");
		for (String word : queryWords) {
			Set<String> wordCands = generateCandidateWords(word, lm);
			if (prevCands != null) {
				
			}
		}

		return candidates;
	}

	private Set<String> generateCandidateWords(String word, LanguageModel lm) {
		Set<String> candidates = new HashSet<String>();
		int wordLen = word.length();
		System.out.println("Word: " + word);

		for (int i = 0; i < wordLen; i++) {
			String deleteWord = null;
			if (i != wordLen - 1) {
				deleteWord = word.substring(0, i) + word.substring(i + 1); // delete
			} else {
				deleteWord = word.substring(0, i);
			}
			
			if (lm.wordExists(deleteWord)) {
				System.out.println("Delete[" + word.charAt(i) + "] : " + deleteWord);
				candidates.add(deleteWord);
			}
			
		}

		for (int i = 0; i < wordLen; i++) {
			for (Character c : alphabet) {
				String insertWord = "";
				String replaceWord = "";
				if (i == 0) {
					insertWord = c + word;
					replaceWord = c + word.substring(1);
				} else if (i == wordLen - 1) {
					insertWord = word + c;
					replaceWord = word.substring(0, i) + c;
				} else {
					insertWord = word.substring(0, i) + c + word.substring(i); // insert
					replaceWord = word.substring(0, i - 1) + c + word.substring(i);
				}

				if (lm.wordExists(insertWord)) {
					candidates.add(insertWord);
					System.out.println("Insert[" + c + "] : " + insertWord);
				}
				
				if (lm.wordExists(replaceWord)) {
					System.out.println("Replace[" + word.charAt(i) + "][" + c + "]: " + replaceWord);
					candidates.add(replaceWord);
				}
				
			}

		}
		for (int i = 0; i < wordLen - 1; i++) {
			String transposeWord = word.substring(0, i) + word.charAt(i + 1) + word.charAt(i)
					+ word.substring(i + 2);
			if (lm.wordExists(transposeWord)) {
				candidates.add(transposeWord);
				System.out.println("Transpose[" + word + "] --> " + transposeWord);
			}
		}

		return candidates;
	}

}
