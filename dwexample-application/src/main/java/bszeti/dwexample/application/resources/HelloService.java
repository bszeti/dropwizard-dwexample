package bszeti.dwexample.application.resources;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import bszeti.dw.example.api.HelloRequest;
import bszeti.dw.example.api.HelloResponse;
import bszeti.dw.example.api.HelloResponseStatus;
import bszeti.dwexample.application.config.Greeting;

@Path(HelloRequest.HELLO_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HelloService {
	private static final Logger log = LoggerFactory.getLogger(HelloService.class);
	
	private Map<String,String> greetings;

	public HelloService(List<Greeting> greetings) {
		//Let's use a map internally
		this.greetings = greetings.stream().collect(Collectors.toMap(Greeting::getLang, Greeting::getPattern));
	}
	
	@GET
	@Path("/{name}")
	public Response sayGreetingsGet(@PathParam("name") String name, @QueryParam("lang") String lang) throws Exception {
		log.info("sayGreetingsGet {} {}",name, lang);
		return sayGreetings(name,lang);
	}
	
	@POST
	@Path("/")
	public Response sayGreetingsPost(@Valid HelloRequest helloRequest) throws Exception{
		log.info("sayGreetingsPost {} {}",helloRequest.getName(), helloRequest.getLang());
		return sayGreetings(helloRequest.getName(),helloRequest.getLang());
	}

	//Helper method for both GET and POST
	private Response sayGreetings(String name, String lang)  throws Exception {
		Map<String,String> answer;
		//Response is created using the name in the greeting patterns
		if (lang == null) {
			//Return all
			answer = greetings.entrySet().stream()
					.collect(
						Collectors.toMap(Map.Entry::getKey, (e)->String.format(e.getValue(), name))
					);
		} else {
			//Return only the lang given. This will throw an exception if lang is not set.
			String pattern = greetings.get(lang);
			if (pattern == null) throw new ConstraintViolationException("Unknow lang",null);
			answer = ImmutableMap.of(lang, String.format(pattern, name));
		}
		
		//Build response. Add server current local time to status
		HelloResponse helloResponse = new HelloResponse(new HelloResponseStatus(0,"OK",LocalDateTime.now()));
		helloResponse.setGreetings(answer);
		return Response.ok()
				.entity(helloResponse)
				.build();
	}
}
