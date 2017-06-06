package bszeti.dw.example.client;

public class ServiceClientException extends Exception{
	
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
