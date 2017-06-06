package bszeti.dwexample.application.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

//An example to have a background service running with several threads
//The ExecutionThreadService represents one thread. They are managed by a ExecutionThreadServiceManager 
public class ExecutionThreadService extends AbstractExecutionThreadService {
	private static final Logger log = LoggerFactory.getLogger(ExecutionThreadService.class);

	private int id;
	private long sleepTime;
	private String serviceName;

	public ExecutionThreadService(int id, long sleepTime) {
		this.id = id;
		this.sleepTime = sleepTime;
		this.serviceName = super.serviceName()+"-"+id; //Thread name: ExecutionThreadService-#
	}
	
	@Override
	protected String serviceName(){
		return this.serviceName;
	}

	@Override
	protected void run() {
		while (isRunning()) {
			try {
				log.info("ExecutionThreadService #{} is running", id);
				Thread.sleep(sleepTime);
			} catch (InterruptedException ex){
				log.debug("Interrupted",ex);
			} catch (Exception ex) {
				// The run() should not throw an exception as it would stop the whole service
				log.error("Error in ExecutionThreadService #{}", id);
			}
		}
		log.info("Done ExecutionThreadService #{}", id);
	}

}
