package bszeti.dw.example.application;


import org.eclipse.jetty.servlet.DefaultServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.SerializationFeature;

import bszeti.dw.example.application.config.DwExampleConfiguration;
import bszeti.dw.example.application.health.BuildInfoHealthCheck;
import bszeti.dw.example.application.health.FileSystemHealthCheck;
import bszeti.dw.example.application.resources.HelloService;
import bszeti.dw.example.application.resources.MultithreadedExecutorService;
import bszeti.dw.example.application.rs.ConstraintViolationExceptionMapper;
import bszeti.dw.example.application.rs.ExceptionMapper;
import bszeti.dw.example.application.rs.JsonProcessingExceptionMapper;
import bszeti.dw.example.application.rs.WebApplicationExceptionMapper;
import bszeti.dw.example.application.task.ExecutionThreadServiceManager;
import bszeti.dw.example.application.task.ScheduledTask;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DwExampleApplication extends Application<DwExampleConfiguration> {
	private static final Logger log = LoggerFactory.getLogger(DwExampleApplication.class);
	
	public static String DEFAULT_TEMPDIR="/tmp";
	
    public static void main(String[] args) throws Exception {
        new DwExampleApplication().run(args);
    }

    @Override
    public String getName() {
        return "dwexample";
    }
    
    @Override
    public void initialize(final Bootstrap<DwExampleConfiguration> bootstrap) {

    }

    @Override
	public void run(DwExampleConfiguration config, Environment environment) throws Exception {
    	//Add health checks
    	environment.healthChecks().register("tempDir", new FileSystemHealthCheck(config.getTempDirPath()));
    	environment.healthChecks().register("buildInfo", new BuildInfoHealthCheck());
		
    	//Modify Jackson object mapper with features.
    	//A Dropwizard style objectmapper can be created using  io.dropwizard.jackson.Jackson.newObjectMapper();
    	environment.getObjectMapper().enable(JsonParser.Feature.IGNORE_UNDEFINED); //Ignore unknown fields
    	environment.getObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE); //For @JsonRootName
    	environment.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //Required for nice LocalDate marshaling
    	environment.getObjectMapper().setSerializationInclusion(Include.NON_NULL); //Skip nulls

    	//Add exception mappers
    	environment.jersey().register(new JsonProcessingExceptionMapper());
    	environment.jersey().register(new ConstraintViolationExceptionMapper());
    	environment.jersey().register(new WebApplicationExceptionMapper());
    	environment.jersey().register(new ExceptionMapper());
    	
    	//Add a new servlet to admin() or servlet()- for example the directory listing default servlet - http://localhost:8081/dir/
    	environment.admin().setInitParameter("org.eclipse.jetty.servlet.Default.resourceBase", config.getTempDirPath());
    	environment.admin().setInitParameter("org.eclipse.jetty.servlet.Default.pathInfoOnly", "true");
    	environment.admin().addServlet("listTempDirServlet", new DefaultServlet()).addMapping("/dir/*");
    	
    	//Add resources
    	environment.jersey().register(new HelloService(config.getGreetings()));
    	environment.jersey().register(new MultithreadedExecutorService(3));
    	
    	//Add background tasks
    	environment.lifecycle().manage(new ScheduledTask(2000));
    	environment.lifecycle().manage(new ExecutionThreadServiceManager(2,2000));
	}
	
	

}
