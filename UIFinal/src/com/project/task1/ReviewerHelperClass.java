package com.project.task1;


import java.io.Serializable;

@SuppressWarnings("rawtypes")
public class ReviewerHelperClass implements Comparable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	double score;
	String name;
	String id;
	String numOfReviews;
	String numden;
	
	public ReviewerHelperClass(String score, String name, String id, String numOfReviews,String numden) {
		this.score = Double.valueOf(score);
		this.name = name;
		this.id = id;
		this.numOfReviews = numOfReviews;
		this.numden = numden;
	}
	
	
	public String getNumden() {
		return numden;
	}

	public void setNumden(String numden) {
		this.numden = numden;
	}

	
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNumOfReviews() {
		return numOfReviews;
	}

	public void setNumOfReviews(String numOfReviews) {
		this.numOfReviews = numOfReviews;
	}

	@Override
	public int compareTo(Object o) {
		ReviewerHelperClass obj2 = (ReviewerHelperClass) o;
		return Double.compare(obj2.getScore(),this.score);
	}

	
	
}
