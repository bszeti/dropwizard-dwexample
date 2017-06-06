package bszeti.dw.example.client;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.client.JerseyClientConfiguration;

public class ServiceClientConfiguration {
	
	@Valid
	@JsonProperty("jerseyClient")
	private JerseyClientConfiguration jerseyClientConfiguration;
	
	@NotEmpty
	@JsonProperty("url")
	private String url;
	
	@JsonProperty("usename")
	private String username;
	
	@JsonProperty("password")
	private String password;

	public JerseyClientConfiguration getJerseyClientConfiguration() {
		return jerseyClientConfiguration;
	}

	public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClientConfiguration) {
		this.jerseyClientConfiguration = jerseyClientConfiguration;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
