package com.shopstyle.snout;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Configuration {

	private static final String GoogleBotUserAgent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";

	private String userAgent;

	private int numThreads;

	private String baseUrl;

	private Map<String, String> altLocaleBaseUrls;

	private List<Test> tests;

	private int maxAttemptsPerTest;

	public Configuration(File configFile, File testsFile) {
		parseConfig(configFile);
		parseTests(testsFile);
	}

	private void parseConfig(File configFile) {
		try{
			String fileContents = FileUtils.readFileToString(configFile);
			JSONObject json = new JSONObject(fileContents);
			baseUrl = json.getString("baseUrl");
			numThreads = json.getInt("numThreads");
			maxAttemptsPerTest = json.getInt("maxAttemptsPerTest");

			// parse the alternate locale base urls
			JSONObject jsonAltLocaleBaseUrls = json.getJSONObject("altLocaleBaseUrls");
			altLocaleBaseUrls = new HashMap<>(jsonAltLocaleBaseUrls.length());
			String[] locales = JSONObject.getNames(jsonAltLocaleBaseUrls);
			for (String locale : locales){
				String altLocaleBaseUrl = jsonAltLocaleBaseUrls.getString(locale);
				altLocaleBaseUrls.put(locale, altLocaleBaseUrl);
			}

			// parse the user agent
			switch (json.getString("userAgent")) {
				case "googlebot" :
				default:
					userAgent = GoogleBotUserAgent;
					break;
			}
		}
		catch(Exception e){
			throw new RuntimeException("Failed to parse json config file", e);
		}
	}

	private void parseTests(File testsFile) {
		try {
			String fileContents = FileUtils.readFileToString(testsFile);
			JSONArray json = new JSONArray(fileContents);
			tests = new ArrayList<>(json.length());

			for (int i = 0; i < json.length(); i++) {
				JSONObject jsonTest = json.getJSONObject(i);
				String url = baseUrl + jsonTest.getString("url");

				String canonical = jsonTest.optString("canonical", null);
				if (canonical != null){
					canonical = baseUrl + canonical;
				}

				String robots = jsonTest.optString("robots", null);
				EnumSet<MetaRobots> robotsSet = null;
				if (robots != null){
					robotsSet = MetaRobots.parse(robots);
				}

				HashMap<String,String> hreflangs = null;
				JSONObject jsonHreflangs = jsonTest.getJSONObject("hreflangs");
				if (jsonHreflangs != null ){
					String[] locales = JSONObject.getNames(jsonHreflangs);
					hreflangs = new HashMap<>(locales.length);
					for (String locale : locales){
						String altLocaleBaseUrl = altLocaleBaseUrls.get(locale);
						String altUrl = altLocaleBaseUrl + jsonHreflangs.getString(locale);
						hreflangs.put(locale, altUrl);
					}
				}

				Test t = new Test();
				t.setUrl(url);
				t.setCanonical(canonical);
				t.setH1(jsonTest.optString("h1", null));
				t.setTitle(jsonTest.optString("title", null));
				t.setHreflangs(hreflangs);
				t.setRobots(robotsSet);
				tests.add(t);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse json tests file", e);
		}
	}

	public int getNumThreads() {
		return numThreads;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public List<Test> getTests(){
		return tests;
	}

	public int getMaxAttemptsPerTest() {
		return maxAttemptsPerTest;
	}

}
