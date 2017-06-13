package bszeti.dwexample.application;

import static org.junit.Assert.*;

import java.net.URI;
import java.time.LocalDateTime;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import bszeti.dw.example.api.HelloRequest;
import bszeti.dw.example.api.HelloResponse;
import bszeti.dw.example.api.HelloResponseStatus;
import bszeti.dw.example.client.ServiceClient;
import bszeti.dw.example.client.ServiceClientConfiguration;
import bszeti.dw.example.client.ServiceClientException;
import bszeti.dwexample.application.config.DwExampleConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;

public class ServiceClientTest {
	private static ServiceClient clientDropwizard;
	private static ServiceClient clientJersey;

	@ClassRule
	public static final DropwizardAppRule<DwExampleConfiguration> RULE = new DropwizardAppRule<>(
			DwExampleApplication.class, ResourceHelpers.resourceFilePath("config-junit.yml"));

	@BeforeClass
	public static void beforeClass() throws Exception {
		//Get url from app context instead of hardcoding it
		URI baseUri = RULE.getEnvironment().getApplicationContext().getServer().getURI();
		String url = baseUri.resolve(RULE.getEnvironment().getApplicationContext().getContextPath()).toString();
		
		//Build config for test
		ServiceClientConfiguration config = new ServiceClientConfiguration();
		config.setUrl(url);
		JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
		jerseyClientConfiguration.setTimeout(Duration.seconds(1));
		jerseyClientConfiguration.setConnectionTimeout(Duration.seconds(1));
		jerseyClientConfiguration.setConnectionRequestTimeout(Duration.seconds(5));
		jerseyClientConfiguration.setKeepAlive(Duration.seconds(5)); //The keepAlive must be less than the server side idleTimeout (usually 30 sec)
		jerseyClientConfiguration.setRetries(2);
		//Async executorservice
		jerseyClientConfiguration.setMinThreads(5);
		jerseyClientConfiguration.setMaxThreads(5);
		jerseyClientConfiguration.setWorkQueueSize(16*1024);
		
		config.setJerseyClientConfiguration(jerseyClientConfiguration);
		
		//Create clients
		clientDropwizard = new ServiceClient(config, RULE.getEnvironment());
		clientJersey = new ServiceClient(url);
	}
	
	@Test
	public void sayGreetingGet() throws Exception {
		HelloResponse response = clientDropwizard.sayGreetingsGet("TEST", "en");
		HelloResponseStatus status = response.getHelloResponseStatus();
		assertEquals(0, status.getCode().intValue());
		assertEquals("OK", status.getMessage());
		assertTrue(java.time.Duration.between(LocalDateTime.now(), status.getTime()).abs().getSeconds()<10);
		assertEquals(1,response.getGreetings().size());
		assertEquals("Hello TEST!", response.getGreetings().get("en"));
	}
	
	@Test
	public void sayGreetingGetJerseyClient() throws Exception {
		HelloResponse response = clientJersey.sayGreetingsGet("TEST", "en");
		HelloResponseStatus status = response.getHelloResponseStatus();
		assertEquals(0, status.getCode().intValue());
		assertEquals("OK", status.getMessage());
		assertTrue(java.time.Duration.between(LocalDateTime.now(), status.getTime()).abs().getSeconds()<10);
		assertEquals(1,response.getGreetings().size());
		assertEquals("Hello TEST!", response.getGreetings().get("en"));
	}
	
	@Test
	public void sayGreetingGetAsync() throws Exception {
		HelloResponse response = clientDropwizard.sayGreetingsGetAsync("TEST", "en").get();
		HelloResponseStatus status = response.getHelloResponseStatus();
		assertEquals(0, status.getCode().intValue());
		assertEquals("OK", status.getMessage());
		assertTrue(java.time.Duration.between(LocalDateTime.now(), status.getTime()).abs().getSeconds()<10);
		assertEquals(1,response.getGreetings().size());
		assertEquals("Hello TEST!", response.getGreetings().get("en"));
	}
	
	@Test
	public void sayGreetingGetAsyncJerseyClient() throws Exception {
		HelloResponse response = clientJersey.sayGreetingsGetAsync("TEST", "en").get();
		HelloResponseStatus status = response.getHelloResponseStatus();
		assertEquals(0, status.getCode().intValue());
		assertEquals("OK", status.getMessage());
		assertTrue(java.time.Duration.between(LocalDateTime.now(), status.getTime()).abs().getSeconds()<10);
		assertEquals(1,response.getGreetings().size());
		assertEquals("Hello TEST!", response.getGreetings().get("en"));
	}
	
	@Test
	public void sayGreetingGetWrongLang() {
		ServiceClientException serviceClientException = null;
		try{
			clientDropwizard.sayGreetingsGet("TEST", "xx");
		} catch (ServiceClientException ex){
			serviceClientException = ex;
		}
		assertEquals(false, serviceClientException.isRecoverable());
		assertEquals("Bad Request", serviceClientException.getMessage());
	}
	
	@Test
	public void sayGreetingGetWrongLangJerseyClient() {
		ServiceClientException serviceClientException = null;
		try{
			clientJersey.sayGreetingsGet("TEST", "xx");
		} catch (ServiceClientException ex){
			serviceClientException = ex;
		}
		assertEquals(false, serviceClientException.isRecoverable());
		assertEquals("Bad Request", serviceClientException.getMessage());
	}
	
	@Test
	public void sayGreetingPost() throws ServiceClientException {
		HelloRequest request = new HelloRequest();
		request.setName("TEST");
		request.setLang("en");
		HelloResponse response = clientDropwizard.sayGreetingsPost(request);
		HelloResponseStatus status = response.getHelloResponseStatus();
		assertEquals(0, status.getCode().intValue());
		assertEquals("OK", status.getMessage());
		assertTrue(java.time.Duration.between(LocalDateTime.now(), status.getTime()).abs().getSeconds()<10);
		assertEquals(1,response.getGreetings().size());
		assertEquals("Hello TEST!", response.getGreetings().get("en"));
	}
	
	@Test
	public void sayGreetingPostJerseyClient() throws ServiceClientException {
		HelloRequest request = new HelloRequest();
		request.setName("TEST");
		request.setLang("en");
		HelloResponse response = clientJersey.sayGreetingsPost(request);
		HelloResponseStatus status = response.getHelloResponseStatus();
		assertEquals(0, status.getCode().intValue());
		assertEquals("OK", status.getMessage());
		assertTrue(java.time.Duration.between(LocalDateTime.now(), status.getTime()).abs().getSeconds()<10);
		assertEquals(1,response.getGreetings().size());
		assertEquals("Hello TEST!", response.getGreetings().get("en"));
	}
	
	@Test
	public void sayGreetingPostWrongLang() {
		ServiceClientException serviceClientException = null;
		HelloRequest request = new HelloRequest();
		request.setName("TEST");
		request.setLang("xx");
		try{
			clientDropwizard.sayGreetingsPost(request);
		} catch (ServiceClientException ex){
			serviceClientException = ex;
		}
		assertEquals(false, serviceClientException.isRecoverable());
		assertEquals("Bad Request", serviceClientException.getMessage());
	}
	
	@Test
	public void sayGreetingPostWrongLangJerseyClient() {
		ServiceClientException serviceClientException = null;
		HelloRequest request = new HelloRequest();
		request.setName("TEST");
		request.setLang("xx");
		try{
			clientJersey.sayGreetingsPost(request);
		} catch (ServiceClientException ex){
			serviceClientException = ex;
		}
		assertEquals(false, serviceClientException.isRecoverable());
		assertEquals("Bad Request", serviceClientException.getMessage());
	}
}
