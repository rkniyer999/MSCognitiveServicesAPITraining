package com.ms.textanalytics;

public class SentimentAnalysisOutput {

	String id;
	String inputtext;
	String language;
	String inputText;
	String sentimentType;
	float sentimentScore;
	String userName;
	
	

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInputtext() {
		return inputtext;
	}

	public void setInputtext(String inputtext) {
		this.inputtext = inputtext;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getInputText() {
		return inputText;
	}

	public void setInputText(String inputText) {
		this.inputText = inputText;
	}

	public String getSentimentType() {
		return sentimentType;
	}

	public void setSentimentType(String sentimentType) {
		this.sentimentType = sentimentType;
	}

	public float getSentimentScore() {
		return sentimentScore;
	}

	public void setSentimentScore(float sentimentScore) {
		this.sentimentScore = sentimentScore;
	}
}
