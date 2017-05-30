package bszeti.dw.example.api;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HelloRequest {
	
	@JsonProperty
	@NotEmpty(message="name is required")
	private String name;
	
	@JsonProperty
	private String lang;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
	
}
