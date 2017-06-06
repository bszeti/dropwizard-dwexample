package bszeti.dw.example.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import bszeti.dw.example.api.HelloRequest;
import bszeti.dw.example.api.HelloResponse;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

/**
 * Helper class to create a client for our service. This can be used in another Java project that calls the service.
 * The client can be managed  
 */
public class ServiceClient {
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
		
		//Create webtarget
		target = client.target(config.getUrl()).path(HelloRequest.HELLO_PATH);
	}
	
	
	/**
	 * Create default Jersey client 
	 */
	public ServiceClient(String url){
		org.glassfish.jersey.client.JerseyClientBuilder builder = new org.glassfish.jersey.client.JerseyClientBuilder();
		
		//Client with default config
		client = org.glassfish.jersey.client.JerseyClientBuilder
				.createClient();
		
		//Create webtarget
		target = client.target(url).path(HelloRequest.HELLO_PATH);
	}
	/**
	 * Use the client injected. It's lifecycle should be managed by the caller.
	 */
	public ServiceClient(String url, Client client){
		this.client = client;
		//Create webtarget
		target = client.target(url).path(HelloRequest.HELLO_PATH);
	}
	
	//TODO: Add client created by Jersey's JerseyClientBuildersay
	
	//Operations - Methods to call the remote http service
	public HelloResponse sayGreetingsGet(String name, String lang) throws ServiceClientException{
		Response response = target
				.path(name)
				.queryParam("lang", lang)
				.request(MediaType.APPLICATION_JSON)
				.get();
		return processResponse(response);
	}
	
	public HelloResponse sayGreetingsPost(HelloRequest request) throws ServiceClientException{
		Response response = target
				.request(MediaType.APPLICATION_JSON)
				.header("myCustomHeader", "value")
				.post(Entity.json(request));
		return processResponse(response);
	}
	
	//Helper method to process response and convert non-200 responses to exception
	private HelloResponse processResponse(Response response) throws ServiceClientException{
		try {
			if (response.getStatusInfo().equals(Response.Status.OK)){
				return response.readEntity(HelloResponse.class);
			} else {
				//If status is not http 200 throw exception
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
			response.close();
		}
	}
	
	//Close the underlying client
	public void close(){
		this.client.close();
	}
	
}
