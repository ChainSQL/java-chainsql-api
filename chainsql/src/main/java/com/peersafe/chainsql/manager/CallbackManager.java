package com.peersafe.chainsql.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.peersafe.base.client.Client;

public class CallbackManager {
	private static CallbackManager single = new CallbackManager();
	
	public static CallbackManager instance() {
		return single;
	}
	
    protected ExecutorService service = Executors.newCachedThreadPool();
    public CallbackManager() {
    }
    
	   /**
     * Run a task.
     * @param runnable Thread object.
     */
    public void runRunnable(Runnable runnable) {
    	try {
    		service.execute(runnable);
    	}catch(Exception e) {
    		e.printStackTrace();
    		if(service.isShutdown()) {
    			Logger.getLogger(CallbackManager.class.getName()).log(Level.WARNING, "service is shutdown,restart now");
    			service = Executors.newCachedThreadPool();
    		}
    	}    	
    }

    public void shutdown(){
		service.shutdown();
	}
}
