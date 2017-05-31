package bszeti.dw.example.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

//If WRAP_ROOT_VALUE is enabled the class name is used by default
public class MultithreadedExecutorResponse {
	@JsonProperty("messages")
	List<String> list;

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}
	
}
