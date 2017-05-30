package bszeti.dw.example.application.task;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ServiceManager;

import io.dropwizard.lifecycle.Managed;

public class ExecutionThreadServiceManager implements Managed {
	private static final Logger log = LoggerFactory.getLogger(ExecutionThreadServiceManager.class);

	private ServiceManager serviceManager;
	
	public ExecutionThreadServiceManager(int threadCount, long sleepTime){
		//Create the given number of services managed by the ServiceManager
		Set<ExecutionThreadService> services = new HashSet<>();
		for (int i=0; i<threadCount; i++){
			services.add(new ExecutionThreadService(i, sleepTime));
		}
		serviceManager = new ServiceManager(services);
	}
	
	//Managed interface so Dropwizard's lifecycle can start/stop the services.
	@Override
	public void start() throws Exception {
		//Start services and wait until it's ok
		serviceManager.startAsync().awaitHealthy();
	}

	@Override
	//The stop() should not throw any exceptions as it terminates the Dropwizard shutdown lifecycle and other managed services are not stopped properly
	public void stop() {
		try {
			serviceManager.stopAsync().awaitStopped();
		} catch (Exception ex) {
			log.error("Failed to stop ExecutionThreadServiceManager",ex);
		}
	}

}
