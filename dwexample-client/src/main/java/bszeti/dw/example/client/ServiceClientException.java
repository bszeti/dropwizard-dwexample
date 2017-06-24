package bszeti.dw.example.client;

public class ServiceClientException extends RuntimeException{
	
	private boolean recoverable = true;
	
	public ServiceClientException(String message) {
		super(message);
	}
	
	public ServiceClientException(String message, boolean recoverable) {
		super(message);
		this.recoverable = recoverable;
	}

	public boolean isRecoverable() {
		return recoverable;
	}

}
