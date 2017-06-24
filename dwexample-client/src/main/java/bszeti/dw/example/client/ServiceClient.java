package bszeti.dw.example.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import bszeti.dw.example.api.HelloRequest;
import bszeti.dw.example.api.HelloResponse;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import jersey.repackaged.com.google.common.base.Function;
import jersey.repackaged.com.google.common.util.concurrent.Futures;
import jersey.repackaged.com.google.common.util.concurrent.ListenableFuture;

/**
 * Helper class to create a client for our service. This can be used in another Java project that calls the service.
 * The client can be managed  
 */
public class ServiceClient {
	private static final Logger log = LoggerFactory.getLogger(ServiceClient.class);
	private static final String NAME = "ServiceClient";
	private Client client;
	private WebTarget target;
	/**
	 * Create client managed by a Dropwizard environment, it can be used in another Dropwizard project
	 */
	public ServiceClient(ServiceClientConfiguration config, Environment environment){
		//Dropwizard's JerseyClientBuilder creates a Jersey client using Apache Http Client
		//For async call it uses the executor service based on the properties in the config, it's started/stopped by the environment
		//The http connection pool is also created based on the proerties in the config 
		JerseyClientBuilder builder = new JerseyClientBuilder(environment);
		
		//Set custom retry handler if needed
		if (config.getJerseyClientConfiguration().getRetries() > 0){
			builder.using(new CustomRequestRetryHandler(config.getJerseyClientConfiguration().getRetries()));
			//For retry the chunked http transfer must be disabled so the request is cached and can be retried
			config.getJerseyClientConfiguration().setChunkedEncodingEnabled(false);
		}
		
		//Build client
		client = builder.using(config.getJerseyClientConfiguration()).build(NAME);
		if (config.getUsername() != null && config.getPassword() != null){
			client.register(HttpAuthenticationFeature.basic(config.getUsername(), config.getPassword()));
		}
		
		// Here we add a custom ThreadPoolExecutor for async calls. It's usually not required.
		// The only difference to default is using ThreadPoolExecutor.CallerRunsPolicy() instead of ThreadPoolExecutor.AbortPolicy()
		ExecutorService asyncExecutor = environment.lifecycle()
				.executorService("jersey-client-MyClient-%d")
				.minThreads(config.getJerseyClientConfiguration().getMinThreads())
				.maxThreads(config.getJerseyClientConfiguration().getMaxThreads())
				.workQueue(new ArrayBlockingQueue<>(config.getJerseyClientConfiguration().getWorkQueueSize()))
				.rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
				.build();
		builder.using(asyncExecutor);		
	
		//Create webtarget
		target = client.target(config.getUrl()).path(HelloRequest.HELLO_PATH);
		
		/* Example POST:
		POST //127.0.0.1:8080/application/hello HTTP/1.1
		Accept: application/json
		myCustomHeader: value
		Content-Type: application/json
		Content-Encoding: gzip
		Transfer-Encoding: chunked
		Host: 127.0.0.1:8080
		Connection: keep-alive
		User-Agent: dwexample (ServiceClient)
		Accept-Encoding: gzip,deflate
		 */
	}
	
	
	/**
	 * Create a Jersey client, no Dropwizard features are used.
	 * 
	 */
	public ServiceClient(String url){
		//Client with custom config
		ClientConfig clientConfig = new ClientConfig();
		
		//Connection settings
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(1, TimeUnit.HOURS); //Keep idle connection in pool
		connectionManager.setMaxTotal(1024); //Default is 20
		connectionManager.setDefaultMaxPerRoute(1024); //Default is 2 only, not OK for production use
		connectionManager.setValidateAfterInactivity(0); //Disable connection validation period (if it's closed on the server side). Might make sense with keepalive
		clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
	
		//Socket/connection timeout
		//For additional details use connectionManager.setDefaultSocketConfig(SocketConfig.custom()...) 
		//and clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, RequestConfig.custom()...)
		clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 500);
		clientConfig.property(ClientProperties.READ_TIMEOUT, 500);
		
		//Use Apache Http client 
		ApacheConnectorProvider apacheConnectorProvider = new  ApacheConnectorProvider();
		clientConfig.connectorProvider(apacheConnectorProvider);
		
		//TODO: How to configure keep alive and connection reuse strategy with Jersey?
		//By default every connection is closed after the request which makes pooling not effective
		//The "Connection: keep-alive" header is added, but the client closes the connection afterwards
		
		//TODO: ExecutorService for async calls must be reviewed for Jersey 2.26
		//ThreadPoolExecutor with given core/max threadcount and an unbounded task queue 
		clientConfig.property(ClientProperties.ASYNC_THREADPOOL_SIZE, 5);
		
		//To disabled chunked encoding and use "Content-Length: ..." instead of "Transfer-Encoding: chunked"
		clientConfig.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
		
		//To Accept-Encoding: gzip,deflate (added by default if EncodingFilter is not used)
		clientConfig.register(GZipEncoder.class);
		clientConfig.register(DeflateEncoder.class);
		
		//To force gzip request encoding for POST
		clientConfig.register(EncodingFilter.class);
		clientConfig.property(ClientProperties.USE_ENCODING, "gzip");
		
		//TODO: Add retry handled using ApacheClientProperties.RETRY_HANDLER with Jersey 2.26
		
		//Configure ObjectMapper
		ObjectMapper objectMapper = new ObjectMapper()
				.registerModule(new JavaTimeModule()) //Support java.time marshaling
				.enable(SerializationFeature.WRAP_ROOT_VALUE) //To marshal with @JsonRootName
				.enable(DeserializationFeature.UNWRAP_ROOT_VALUE) //To unmarshal with @JsonRootName
				;
		clientConfig.register(new JacksonJaxbJsonProvider(objectMapper,null));

		//ClientBuilder uses org.glassfish.jersey.client.JerseyClientBuilder
		client = ClientBuilder.newClient(clientConfig);

		//Create webtarget
		target = client.target(url).path(HelloRequest.HELLO_PATH);
		
		/* Example POST:
		POST //127.0.0.1:8080/application/hello HTTP/1.1
		Accept: application/json
		myCustomHeader: value
		Content-Type: application/json
		Accept-Encoding: deflate,gzip,x-gzip
		Content-Encoding: gzip
		User-Agent: Jersey/2.25.1 (Apache HttpClient 4.5.3)
		Content-Length: 62
		Host: 127.0.0.1:8080
		Connection: keep-alive
		*/
	}
	
	/**
	 * Use the client injected. It's lifecycle should be managed by the caller.
	 */
	public ServiceClient(String url, Client client){
		this.client = client;
		//Create webtarget
		target = client.target(url).path(HelloRequest.HELLO_PATH);
	}
	
	
	//Operations - Methods to call the remote http service
	public HelloResponse sayGreetingsGet(String name, String lang) throws ServiceClientException{
		Response response = target
				.path(name)
				.queryParam("lang", lang)
				.request(MediaType.APPLICATION_JSON)
				.get();
		return processResponse(response);
	}
	
	//Async request
	public Future<HelloResponse> sayGreetingsGetAsync(String name, String lang) {
		Future<Response> futureResponse = target
				.path(name)
				.queryParam("lang", lang)
				.request(MediaType.APPLICATION_JSON)
				.async()
				.get();
		
		//Process the Response to HelloResponse when .get() is called. This code is executed by the thread calling Future.get()
		//checked exceptions are not allowed
		return Futures.lazyTransform(futureResponse, (r)-> processResponse(r));
	}
	
	//Async request - forcing ListenableFuture, which is actually returned by Jersey async
	public Future<HelloResponse> sayGreetingsGetAsyncListenableFuture(String name, String lang) {
		ListenableFuture<Response> futureResponse = (ListenableFuture<Response>) target
				.path(name)
				.queryParam("lang", lang)
				.request(MediaType.APPLICATION_JSON)
				.async()
				.get();
		
		//Process the Response to HelloResponse when the Future is completed successfully. The transform function is executed by the thread from the client's pool.
		return Futures.transform(futureResponse, processResponseFunction);
	}
	
	public HelloResponse sayGreetingsPost(HelloRequest request) throws ServiceClientException{
		Response response = target
				.request(MediaType.APPLICATION_JSON)
				.header("myCustomHeader", "value")
				.post(Entity.json(request));
		return processResponse(response);
	}
	
	private static Function<Response,HelloResponse> processResponseFunction = new Function<Response,HelloResponse>(){

		@Override
		public HelloResponse apply(Response response) {
			return processResponse(response);
		}
		
	};
	
	//Helper method to process response and convert non-200 responses to exception
	private static HelloResponse processResponse(Response response) throws ServiceClientException{
		log.info("Processing response");
		try {
			if (response.getStatusInfo().equals(Response.Status.OK)){
				return response.readEntity(HelloResponse.class);
			} else {
				//If status is not http 200 throw exception
				String body = response.readEntity(String.class); //response.close() has a bug, it's safer to read the stream, though we don't use it here 
				String message = response.getStatusInfo().toString();
				if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SERVER_ERROR)){
					//Let's assume that http 5xx is recoverable and the client should retry
					throw new ServiceClientException(message, true);
				} else {
					throw new ServiceClientException(message, false);
				}
			}
		} finally {
			//Close response object to close underlying stream if it's not fully read.
			//See related bug https://github.com/jersey/jersey/issues/3505
			response.close();
		}
	}
	
	//Close the underlying client
	public void close(){
		this.client.close();
	}
	
}
