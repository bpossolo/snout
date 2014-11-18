package com.shopstyle.snout;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TestRunner implements Runnable {

	private final Logger log;
	private final Configuration config;
	private final Queue<Test> queue;
	private final CountDownLatch latch;

	private static final String NewLinePadding = "\n  ";

	public TestRunner(Configuration config, Queue<Test> queue, CountDownLatch latch, Handler handler) throws IOException {
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

	void doTest(Test test) throws IOException {
		String name = test.getName();
		String url = test.getUrl();
		log.log(Level.INFO, "Executing test: {0}", (name == null) ? url : name);
		String userAgent = config.getUserAgent();
		Document doc = Jsoup.connect(url).userAgent(userAgent).followRedirects(true).get();
		Element head = doc.head();
		verifyCanonical(head, test);
		verifyHreflangs(head, test);
		verifyMetaRobots(head, test);
		verifyTitle(head, test);
		verifyH1(doc, test);
	}

	private void verifyCanonical(Element head, Test test){
		String expected = test.getCanonical();
		Elements elements = head.select("link[rel=canonical]");
		verify(expected, elements, ElementType.Canonical);
	}

	private void verifyTitle(Element head, Test test){
		String expected = test.getTitle();
		Elements elements = head.select("title");
		verify(expected, elements, ElementType.Title);
	}

	private void verifyH1(Document doc, Test test){
		String expected = test.getH1();
		Elements elements = doc.select("h1");
		verify(expected, elements, ElementType.H1);
	}

	private void verifyHreflangs(Element head, Test test){
		Map<String,String> hreflangs = test.getHreflangs();
		for (Locale locale : config.getLocales()){
			String expected = hreflangs.get(locale.toLanguageTag());
			Elements elements = head.select("link[rel=alternate][hreflang=" + locale.toLanguageTag() + "]");
			verify(expected, elements, ElementType.Hreflang);
		}
	}

	private void verifyMetaRobots(Element head, Test test){
		EnumSet<MetaRobots> expected = test.getRobots();
		Elements elements = head.select("meta[name=robots]");
		verify(expected, elements, ElementType.Robots);
	}

	private void verify(Object expected, Elements elements, ElementType type){
		int numMatches = elements.size();

		if (expected == null){
			if (numMatches > 0){
				unexpectedElements(elements, type);
			}
		}
		else {
			if (numMatches == 0){
				insufficientElements(expected, type);
			}
			else if (numMatches == 1){
				Object actual = type.getElementValue(elements.first());
				compare(type, expected, actual);
			}
			else {
				tooManyElements(expected, elements, type);
			}
		}
	}

	private void compare(ElementType type, Object expected, Object actual){
		if (!expected.equals(actual)){
			log.log(Level.WARNING, "Expected {0} [{1}] but found [{2}]", new Object[]{type, expected, actual});
		}
	}

	private void insufficientElements(Object expected, ElementType type){
		log.log(Level.WARNING, "Expected 1 {0} [{1}] but found 0", new Object[]{type, expected});
	}

	private void unexpectedElements(Elements elements, ElementType type){
		StringBuilder sb = new StringBuilder("Did not expect a ").append(type).append(" but found:");
		for (int i = 0; i < elements.size(); i++) {
			Object value = type.getElementValue(elements.get(i));
			sb.append(NewLinePadding).append(value);
		}
		log.warning(sb.toString());
	}

	private void tooManyElements(Object expected, Elements elements, ElementType type){
		StringBuilder sb = new StringBuilder();
		sb.append("Expected 1 ").append(type).append(" [").append(expected).append("] but found multiple:");
		for (int i = 0; i < elements.size(); i++){
			Object value = type.getElementValue(elements.get(i));
			sb.append(NewLinePadding).append(value);
		}
		log.warning(sb.toString());
	}


}
