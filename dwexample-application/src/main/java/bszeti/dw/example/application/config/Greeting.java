package bszeti.dw.example.application.config;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Greeting {
	@JsonProperty
	@NotEmpty
	private String lang;
	
	@JsonProperty
	@Pattern(regexp = ".*%s.*", message="The pattern must contains at least one '%s' for the name")
	private String pattern;

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	
}
