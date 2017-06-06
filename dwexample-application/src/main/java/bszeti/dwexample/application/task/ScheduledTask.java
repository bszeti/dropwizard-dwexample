package bszeti.dwexample.application.task;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;

import io.dropwizard.lifecycle.Managed;

//Simple scheduled task
public class ScheduledTask extends AbstractScheduledService implements Managed {
	private static final Logger log = LoggerFactory.getLogger(ScheduledTask.class);
	
	private long period;
	
	public ScheduledTask(long period) {
		this.period = period;
	}
	

	@Override
	//Make sure not to throw any exceptions in this method as an exception stops the whole task and it won't run again
	protected void runOneIteration() {
		try {
			log.info("Background task isrunning.");
		} catch (Exception ex) {
			log.error("Error in ScheduledTask", ex);
		}
	}

	@Override
	protected Scheduler scheduler() {
		//Periodic run. Also see Scheduler.newFixedDelaySchedule()
		return Scheduler.newFixedRateSchedule(0, period, TimeUnit.MILLISECONDS);
	}

	//Managed interface so Dropwizard can manage the task's lifecycle
	@Override
	public void start() throws Exception {
		//Start task and wait until running state so errors will stop the Dropwizard startup. Otherwise the error might not be noticed.
		this.startAsync().awaitRunning();
	}

	@Override
	//The stop() should not throw any exceptions as it terminates the Dropwizard shutdown lifecycle and other managed services are not stopped properly
	public void stop() {
		try {
			this.stopAsync().awaitTerminated();
			log.info("ScheduledTask is stopped.");
		} catch (IllegalStateException ex) {
			//If the service couldn't start this exception is thrown, but it's expected
			log.warn("Failed to stop ScheduledTask as the service probably didn't start properly.");
		} catch (Exception ex) {
			log.error("Failed to stop ScheduledTask",ex);
		}

	}


}
