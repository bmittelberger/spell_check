package edu.stanford.cs276.util;

import java.util.ArrayList;


public class PossibleQuery implements Comparable<PossibleQuery> {
	private double score;
	
	private ArrayList<Pair < String, String> > query;
	
	public PossibleQuery(Pair<String, String> sourceWord) {
		this.score = 0.0;
		this.query = new ArrayList<Pair < String, String> >();
		this.query.add(sourceWord);
	}
	
	public PossibleQuery(ArrayList<Pair< String, String> > query) {
		this.score = 0.0;
		this.query = query;
	}
	
	public ArrayList<Pair < String, String> > getQuery() {
		return query;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public int compareTo(PossibleQuery o) {
		if (Double.isNaN(this.score)) {
			return 1;
		}
		if (this.score < o.getScore()) {
			return 1;
		} 
		return -1;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (Pair<String, String> pr : this.query) {
			  sb.append(pr.toString() + ", ");
		  }
		  sb.append("] -- Score: " + Double.toString(this.score));
		  return sb.toString();
	}
	
	
	
}
