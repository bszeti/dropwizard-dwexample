package bszeti.dwexample.application.config;

import static bszeti.dwexample.application.DwExampleApplication.DEFAULT_TEMPDIR;

import java.util.List;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class DwExampleConfiguration extends Configuration {
	
	@JsonProperty("greetings")
	@NotEmpty(message = "Add at least one greeting pattern")
	@Valid
	private List<Greeting> greetings;
	
	@JsonProperty("tempDir")
	@NotEmpty
	private String tempDirPath = DEFAULT_TEMPDIR;

	public String getTempDirPath() {
		return tempDirPath;
	}

	public void setTempDirPath(String tempDirPath) {
		this.tempDirPath = tempDirPath;
	}

	public List<Greeting> getGreetings() {
		return greetings;
	}

	public void setGreetings(List<Greeting> greetings) {
		this.greetings = greetings;
	}
	
	

}
