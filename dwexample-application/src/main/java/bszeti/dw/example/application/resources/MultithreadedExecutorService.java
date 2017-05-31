package bszeti.dw.example.application.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import bszeti.dw.example.api.MultithreadedExecutorResponse;

@Path("executor")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MultithreadedExecutorService {
	private static Logger log = LoggerFactory.getLogger(MultithreadedExecutorService.class);
	private static String THREAD_NAME = "MultithreadedExecutorService-%d";
	private ListeningExecutorService listeningExecutorService;
	
	//We can add this callback to listenable futures
	private final static FutureCallback<String> callback = new FutureCallback<String>() {

		@Override
		public void onSuccess(String result) {
			log.info("Done");
		}

		@Override
		public void onFailure(Throwable t) {
			log.error("Failed");
		}
		
	};
	
	public MultithreadedExecutorService(int threadCount) {
		//Create executor service with thread name
		ExecutorService executor = Executors.newFixedThreadPool(threadCount, new ThreadFactoryBuilder().setNameFormat(THREAD_NAME).build());
		//We can use ListeningExecutorService to add callbacks. If not needed we could use default java Future
		listeningExecutorService = MoreExecutors.listeningDecorator(executor);
	}
	
	@GET
	@Path("/{count}")
	public Response runwithExecutorPost(@PathParam("count") int count) throws Exception{
		//Collect the futures in a list. Think about the memory impact if it's long.
		List<ListenableFuture<String>> futures = new ArrayList<>();
		for (int i=0; i<count; i++){
			Integer id = new Integer(i);
			ListenableFuture<String> future = listeningExecutorService.submit(() -> {
				return "Response #"+id+" by "+Thread.currentThread().getName();
			});
			Futures.addCallback(future, callback);
			futures.add(future);
		}
		
		//Get the result of all tasks. If any of the tasks failed an exception will be thrown
		List<String> list = Futures.allAsList(futures).get();
		
		MultithreadedExecutorResponse response = new MultithreadedExecutorResponse();
		response.setList(list);
		
		return Response.ok(response).build();
	}
}
