package com.shopstyle.snout;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestOrchestrator {

	private static final Logger log = Logger.getLogger(TestOrchestrator.class.getName());

	private final Configuration config;

	public TestOrchestrator(Configuration config){
		this.config = config;
	}

	public void go() throws Exception {

		ConcurrentLinkedQueue<Test> queue = new ConcurrentLinkedQueue<Test>(config.getTests());
		int numThreads = config.getNumThreads();
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		CountDownLatch latch = new CountDownLatch(numThreads);

		log.log(Level.INFO, "Launching {0} test runners", numThreads);

		// create the test runners
		for (int i = 0; i < numThreads; i++){
			Handler handler = null;
			if (config.logOutputToConsole()){
				handler = new ConsoleHandler();
			}
			else {
				// assign a unique file logger to each test runner/thread
				String logFilenamePatter = "%t/snout-" + i + ".%g.log";
				handler = new FileHandler(logFilenamePatter);
			}
			TestRunner runner = new TestRunner(config, queue, latch, handler);
			executor.execute(runner);
		}

		latch.await();
		executor.shutdown();
		log.info("All test runners have finished");
	}

}
