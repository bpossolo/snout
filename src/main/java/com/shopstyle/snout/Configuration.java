package com.shopstyle.snout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {

	private static final String GoogleBotUserAgent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";

	private String locale;
	private String userAgent;
	private int numThreads;
	private Set<Locale> locales;
	private String baseUrl;
	private Map<String, String> baseUrls;
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
			numThreads = json.getInt("numThreads");
			maxAttemptsPerTest = json.getInt("maxAttemptsPerTest");

			// parse the alternate locale base urls
			JSONObject jsonAltLocaleBaseUrls = json.getJSONObject("baseUrls");
			locales = new HashSet<>(jsonAltLocaleBaseUrls.length());
			baseUrls = new HashMap<>(jsonAltLocaleBaseUrls.length());
			String[] languageTags = JSONObject.getNames(jsonAltLocaleBaseUrls);
			for (String languageTag : languageTags){
				locales.add(Locale.forLanguageTag(languageTag));
				String altLocaleBaseUrl = jsonAltLocaleBaseUrls.getString(languageTag);
				baseUrls.put(languageTag, altLocaleBaseUrl);
			}

			// parse the user agent
			switch (json.getString("userAgent")) {
				case "googlebot" :
				default:
					userAgent = GoogleBotUserAgent;
					break;
			}
		}
		catch(JSONException | IOException e){
			throw new RuntimeException("Failed to parse json config file", e);
		}
	}

	private void parseTests(File testsFile) {
		try {
			String fileContents = FileUtils.readFileToString(testsFile);
			JSONObject json = new JSONObject(fileContents);
			baseUrl = baseUrls.get(json.getString("locale"));
			if (baseUrl == null){
				throw new RuntimeException("BaseUrls config property must contain entry for locale key [" + locale + "]");
			}

			JSONArray jsonTests = json.getJSONArray("tests");
			tests = new ArrayList<>(jsonTests.length());

			for (int i = 0; i < jsonTests.length(); i++) {
				JSONObject jsonTest = jsonTests.getJSONObject(i);
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

				Map<String,String> hreflangs = null;
				JSONObject jsonHreflangs = jsonTest.optJSONObject("hreflangs");
				if (jsonHreflangs == null ){
					hreflangs = Collections.emptyMap();
				}
				else {
					String[] locales = JSONObject.getNames(jsonHreflangs);
					hreflangs = new HashMap<>(locales.length);
					for (String locale : locales){
						String altLocaleBaseUrl = baseUrls.get(locale);
						String altUrl = altLocaleBaseUrl + jsonHreflangs.getString(locale);
						hreflangs.put(locale, altUrl);
					}
				}

				Test t = new Test();
				t.setName(jsonTest.optString("name"));
				t.setUrl(url);
				t.setCanonical(canonical);
				t.setH1(jsonTest.optString("h1", null));
				t.setTitle(jsonTest.optString("title", null));
				t.setHreflangs(hreflangs);
				t.setRobots(robotsSet);
				tests.add(t);
			}
		}
		catch (JSONException | IOException e) {
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

	public Map<String, String> getAltLocaleBaseUrls() {
		return baseUrls;
	}

	public Set<Locale> getLocales(){
		return locales;
	}

}
