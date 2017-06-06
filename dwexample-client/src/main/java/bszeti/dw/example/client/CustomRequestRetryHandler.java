package bszeti.dw.example.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;

import javax.net.ssl.SSLException;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

/**
 * The DefaultHttpRequestRetryHandler doesn't retry in case of InterruptedIOException, UnknownHostException, ConnectException and SSLException.
 * This class overwrites the list of non-retried exceptions if it makes sense to retry
 */
public class CustomRequestRetryHandler extends DefaultHttpRequestRetryHandler {
	protected static final Collection<Class<? extends IOException>> ignoredExceptions = Arrays.asList(UnknownHostException.class, SSLException.class);
	
	public CustomRequestRetryHandler(final int retryCount) {
		super(retryCount, true, ignoredExceptions);
	}

}
