package com.ms.textanalytics;

import java.util.UUID;

/**
 * POJO for translated text output.
 */

public class TextTranslationOutput {
	String id;
	String inputtext;
	String translatedtext_german;
	String translatedtext_italian;
	String user;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getId() {
		UUID uuid = UUID.randomUUID();
		return id = uuid.toString();
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

	public String getTranslatedtext_german() {
		return translatedtext_german;
	}

	public void setTranslatedtext_german(String translatedtext_german) {
		this.translatedtext_german = translatedtext_german;
	}

	public String getTranslatedtext_italian() {
		return translatedtext_italian;
	}

	public void setTranslatedtext_italian(String translatedtext_italian) {
		this.translatedtext_italian = translatedtext_italian;
	}
}