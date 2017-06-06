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
		jerseyClientConfiguration.setTimeout(Duration.seconds(10));
		config.setJerseyClientConfiguration(jerseyClientConfiguration);
		
		//Create clients
		clientDropwizard = new ServiceClient(config, RULE.getEnvironment());
		clientJersey = new ServiceClient(url);
	}
	
	@Test
	public void sayGreetingGet() throws ServiceClientException {
		HelloResponse response = clientDropwizard.sayGreetingsGet("TEST", "en");
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
}
