package com.project.mr;

import java.io.Serializable;

public class Options implements Serializable {

	private static final long serialVersionUID = 1L;
	private String title;
	private String genre;
	private Double amazonscore;
	private String awards;
	private String poster;
	private Double imdbrating;
	private String language;
	private String year;

	public Options( String title, String genre, Double amazonscore, String awards, String poster, Double imdbrating,
			String language, String year){
		this.title=title;
		this.genre=genre;
		this.amazonscore=amazonscore;
		this.awards=awards;
		this.poster=poster;
		this.imdbrating=imdbrating;
		this.language=language;
		this.year=year;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public Double getAmazonscore() {
		return amazonscore;
	}

	public void setAmazonscore(Double amazonscore) {
		this.amazonscore = amazonscore;
	}

	public String getAwards() {
		return awards;
	}

	public void setAwards(String awards) {
		this.awards = awards;
	}

	public String getPoster() {
		return poster;
	}

	public void setPoster(String poster) {
		
			this.poster = poster;
		
	}

	public Double getImdbrating() {
		return imdbrating;
	}

	public void setImdbrating(Double imdbrating) {
		this.imdbrating = imdbrating;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

    
}