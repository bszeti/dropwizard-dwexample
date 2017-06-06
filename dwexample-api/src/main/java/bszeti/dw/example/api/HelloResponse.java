package bszeti.dw.example.api;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("helloResponse")
public class HelloResponse {
	
	@JsonProperty("status")
	private HelloResponseStatus helloResponseStatus;
	
	@JsonProperty
	Map<String,String> greetings;
	
	public HelloResponse() {
		//Default constructor is needed by Jackson for unmarshaling
	}
	
	public HelloResponse(HelloResponseStatus helloResponseStatus) {
		this.helloResponseStatus = helloResponseStatus;
	}

	public HelloResponseStatus getHelloResponseStatus() {
		return helloResponseStatus;
	}

	public void setHelloResponseStatus(HelloResponseStatus helloResponseStatus) {
		this.helloResponseStatus = helloResponseStatus;
	}

	public Map<String, String> getGreetings() {
		return greetings;
	}

	public void setGreetings(Map<String, String> greetings) {
		this.greetings = greetings;
	}
	
	

}
