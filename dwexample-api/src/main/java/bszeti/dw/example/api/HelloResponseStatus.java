package bszeti.dw.example.api;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HelloResponseStatus {
	
	@JsonProperty
	private Integer code;
	@JsonProperty
	private String message;
	@JsonProperty
	LocalDateTime time;
	
	public HelloResponseStatus(Integer code, String message, LocalDateTime time){
		this.code=code;
		this.message = message;
		this.time = time;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}
	
	
}
