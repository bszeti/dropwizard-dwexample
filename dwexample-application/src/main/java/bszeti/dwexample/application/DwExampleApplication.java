package bszeti.dwexample.application;


import org.eclipse.jetty.servlet.DefaultServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;

import bszeti.dwexample.application.config.DwExampleConfiguration;
import bszeti.dwexample.application.health.BuildInfoHealthCheck;
import bszeti.dwexample.application.health.FileSystemHealthCheck;
import bszeti.dwexample.application.resources.HelloService;
import bszeti.dwexample.application.resources.MultithreadedExecutorService;
import bszeti.dwexample.application.rs.ConstraintViolationExceptionMapper;
import bszeti.dwexample.application.rs.ExceptionMapper;
import bszeti.dwexample.application.rs.JsonProcessingExceptionMapper;
import bszeti.dwexample.application.rs.WebApplicationExceptionMapper;
import bszeti.dwexample.application.task.ExecutionThreadServiceManager;
import bszeti.dwexample.application.task.ScheduledTask;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
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
    	//Serve files at /src/main/resources/assets at url /[applicationContext]/static
    	//The asset servlet mapping path must be different than for Jersey resources (which "/") 
    	//The third parameter is the default file to serve
    	bootstrap.addBundle(new AssetsBundle("/assets", "/static", "index.html"));

    }

    @Override
	public void run(DwExampleConfiguration config, Environment environment) throws Exception {
    	//It's possible to change the url prefix for all the registered Jersey resources. This has no impact on assets url.
    	//The resources would be served at [applicationContext]/api/...
    	//In might be useful to strictly separate APIs from assets under a different url path.
    	//The default "/" works well in this case.
    	//environment.jersey().setUrlPattern("/api");
    	
    	//Add health checks
    	environment.healthChecks().register("tempDir", new FileSystemHealthCheck(config.getTempDirPath()));
    	environment.healthChecks().register("buildInfo", new BuildInfoHealthCheck());
		
    	//Modify Jackson object mapper with features.
    	//A Dropwizard style objectmapper can be created using  io.dropwizard.jackson.Jackson.newObjectMapper();
    	environment.getObjectMapper().enable(JsonParser.Feature.IGNORE_UNDEFINED); //Ignore unknown fields
    	environment.getObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE); //To marshal with  @JsonRootName
    	environment.getObjectMapper().enable(DeserializationFeature.UNWRAP_ROOT_VALUE); //To unmarshal with @JsonRootName
    	environment.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //Required for nice Date marshaling
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
