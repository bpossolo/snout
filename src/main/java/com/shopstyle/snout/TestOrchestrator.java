package com.shopstyle.snout;

import java.text.MessageFormat;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestOrchestrator {

	private static final Logger log = Logger.getLogger(TestOrchestrator.class.getName());

	private final Configuration config;

	public TestOrchestrator(Configuration config){
		this.config = config;
	}

	public void go() throws Exception {

		ConcurrentLinkedQueue<Test> taskQueue = new ConcurrentLinkedQueue<>(config.getTests());
		ConcurrentLinkedQueue<Test> failureQueue = new ConcurrentLinkedQueue<>();
		int numThreads = config.getNumThreads();
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		CountDownLatch latch = new CountDownLatch(numThreads);

		log.log(Level.INFO, "Launching {0} test runners", numThreads);

		// create the test runners
		for (int i = 0; i < numThreads; i++){
			TestRunner runner = new TestRunner(config, taskQueue, failureQueue, latch);
			executor.execute(runner);
		}

		latch.await();
		executor.shutdown();
		log.info("All test runners have finished");

		logResults(failureQueue);
	}

	private void logResults(Queue<Test> failureQueue){
		int attempted = config.getTests().size();
		int failed = failureQueue.size();
		int passed = attempted - failed;

		String stats = MessageFormat.format("{0}/{1} tests passed\n\n", passed, attempted);

		StringBuilder sb = new StringBuilder(stats);

		for (Test failedTest : failureQueue){
			sb.append("Test Failure: ").append(failedTest.getName()).append('\n');
			for (Failure f : failedTest.getFailures()){
				sb.append("  ").append(f.getMsg()).append('\n');
			}
			sb.append('\n');
		}

		log.info(sb.toString());
	}

}
