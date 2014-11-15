package com.shopstyle.snout;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TestRunner implements Runnable {

	private final Logger log;

	private final Configuration config;

	private final Queue<Test> queue;

	private final CountDownLatch latch;

	public TestRunner(Configuration config, Queue<Test> queue, CountDownLatch latch, FileHandler handler) throws IOException {
		this.config = config;
		this.queue = queue;
		this.latch = latch;
		log = Logger.getLogger(TestRunner.class.getName());
		log.setUseParentHandlers(false);
		log.addHandler(handler);
	}

	@Override
	public void run() {
		while (!queue.isEmpty()){
			Test test = queue.poll();
			try{
				doTest(test);
			}
			catch(Exception e){
				log.log(Level.WARNING, "Test " + test.getUrl() + " failed", e);
				if (test.getAttempts() < config.getMaxAttemptsPerTest()){
					test.incrementAttempts();
					queue.add(test);
				}
				else {
					log.log(Level.WARNING, "Abandoning test " + test.getUrl());
				}
			}
		}
		latch.countDown();
	}

	private void doTest(Test test) throws IOException {
		String url = test.getUrl();
		String userAgent = config.getUserAgent();
		Document doc = Jsoup.connect(url).userAgent(userAgent).followRedirects(true).get();
		Element head = doc.head();
		// TODO
	}
}
