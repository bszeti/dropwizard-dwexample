package bszeti.dw.example.application.rs;

import java.time.LocalDateTime;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bszeti.dw.example.api.HelloResponse;
import bszeti.dw.example.api.HelloResponseStatus;

//Return our response object in case of exception
public class WebApplicationExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<WebApplicationException>{
	private static final Logger log = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

	@Override
	public Response toResponse(WebApplicationException exception) {
		log.error("WebApplicationException",exception);
		return Response
				.status(exception.getResponse().getStatus())
				.entity(new HelloResponse(new HelloResponseStatus(9,exception.getMessage(),LocalDateTime.now())))
				.build();
	}
}
