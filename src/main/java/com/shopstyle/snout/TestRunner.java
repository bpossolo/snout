package com.shopstyle.snout;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TestRunner implements Runnable {

	private final Configuration config;
	private final Queue<Test> taskQueue;
	private final Queue<Test> failureQueue;
	private final CountDownLatch latch;

	public TestRunner(Configuration config, Queue<Test> taskQueue, Queue<Test> failureQueue, CountDownLatch latch) throws IOException {
		this.config = config;
		this.taskQueue = taskQueue;
		this.failureQueue = failureQueue;
		this.latch = latch;
	}

	@Override
	public void run() {
		while (!taskQueue.isEmpty()){
			Test test = taskQueue.poll();
			if (test != null){ // queue may return null if it's empty
				doTest(test);
			}
		}
		latch.countDown();
	}

	void doTest(Test test) {
		String url = test.getUrl();
		try{
			String userAgent = config.getUserAgent();
			Document doc = Jsoup.connect(url).userAgent(userAgent).followRedirects(true).get();
			Element head = doc.head();
			verifyCanonical(head, test);
			verifyHreflangs(head, test);
			verifyMetaRobots(head, test);
			verifyTitle(head, test);
			verifyH1(doc, test);
		}
		catch(IOException e){
			if (test.getAttempts() < config.getMaxAttemptsPerTest()){
				test.incrementAttempts();
				taskQueue.add(test);
			}
			else {
				Failure f = Failure.io(url);
				test.addFailure(f);
			}
		}
		finally {
			if (test.hasFailures()){
				failureQueue.add(test);
			}
		}
	}

	private void verifyCanonical(Element head, Test test){
		String expected = test.getCanonical();
		Elements elements = head.select("link[rel=canonical]");
		verify(test, expected, elements, ElementType.Canonical);
	}

	private void verifyTitle(Element head, Test test){
		String expected = test.getTitle();
		Elements elements = head.select("title");
		verify(test, expected, elements, ElementType.Title);
	}

	private void verifyH1(Document doc, Test test){
		String expected = test.getH1();
		Elements elements = doc.select("h1");
		verify(test, expected, elements, ElementType.H1);
	}

	private void verifyHreflangs(Element head, Test test){
		Map<String,String> hreflangs = test.getHreflangs();
		for (Locale locale : config.getLocales()){
			String expected = hreflangs.get(locale.toLanguageTag());
			Elements elements = head.select("link[rel=alternate][hreflang=" + locale.toLanguageTag() + "]");
			verify(test, expected, elements, ElementType.Hreflang);
		}
	}

	private void verifyMetaRobots(Element head, Test test){
		EnumSet<MetaRobots> expected = test.getRobots();
		Elements elements = head.select("meta[name=robots]");
		verify(test, expected, elements, ElementType.Robots);
	}

	private void verify(Test test, Object expected, Elements elements, ElementType type){
		int numMatches = elements.size();
		Failure failure = null;

		if (expected == null){
			if (numMatches > 0){
				List<Object> actual = type.getElementValues(elements);
				failure = Failure.unexpectedElements(actual, type);
			}
		}
		else {
			if (numMatches == 0){
				failure = Failure.insufficientElements(expected, type);
			}
			else if (numMatches == 1){
				Object actual = type.getElementValue(elements.first());
				if (!expected.equals(actual)){
					failure = Failure.comparison(expected, actual, type);
				}
			}
			else {
				List<Object> actual = type.getElementValues(elements);
				failure = Failure.tooManyElements(expected, actual, type);
			}
		}

		if (failure != null){
			test.addFailure(failure);
		}
	}

}
