package com.peersafe.chainsql.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class CallbackManager {
	private static CallbackManager single = new CallbackManager();
	
	public static CallbackManager instance() {
		return single;
	}
	
    protected ExecutorService service = Executors.newCachedThreadPool();
    protected Thread publishThread;
    public CallbackManager() {
    }
    
	   /**
     * Run a task.
     * @param runnable Thread object.
     */
    public void runRunnable(Runnable runnable) {
    	service.execute(runnable);
    }    
}
