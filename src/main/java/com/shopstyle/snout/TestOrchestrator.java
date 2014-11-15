package com.shopstyle.snout;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;

public class TestOrchestrator {

	private final Configuration config;

	public TestOrchestrator(Configuration config){
		this.config = config;
	}

	public void go() throws Exception {

		ConcurrentLinkedQueue<Test> queue = new ConcurrentLinkedQueue<Test>(config.getTests());
		int numThreads = config.getNumThreads();
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		CountDownLatch latch = new CountDownLatch(numThreads);

		// create the test runners and assign a unique file logger to each one
		for (int i = 0; i < numThreads; i++){
			String logFilenamePatter = "%t/snout-" + i + ".%g.log";
			FileHandler handler = new FileHandler(logFilenamePatter);
			TestRunner runner = new TestRunner(config, queue, latch, handler);
			executor.execute(runner);
		}

		latch.await();
		executor.shutdown();
	}

}
