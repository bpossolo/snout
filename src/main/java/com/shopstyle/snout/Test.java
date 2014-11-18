package com.shopstyle.snout;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class Test {

	private String name;
	private int attempts = 0;
	private String url;
	private String canonical;
	private String title;
	private String h1;
	private EnumSet<MetaRobots> robots;
	private Map<String, String> hreflangs;
	private List<Failure> failures;

	public String getName() {
		return (name == null) ? url : name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAttempts() {
		return attempts;
	}

	public void incrementAttempts(){
		attempts++;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCanonical() {
		return canonical;
	}

	public void setCanonical(String canonical) {
		this.canonical = canonical;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getH1() {
		return h1;
	}

	public void setH1(String h1) {
		this.h1 = h1;
	}

	public Map<String, String> getHreflangs() {
		return hreflangs;
	}

	public void setHreflangs(Map<String, String> hreflangs) {
		this.hreflangs = hreflangs;
	}

	public EnumSet<MetaRobots> getRobots() {
		return robots;
	}

	public void setRobots(EnumSet<MetaRobots> robots) {
		this.robots = robots;
	}

	public List<Failure> getFailures() {
		return failures;
	}

	public boolean hasFailures(){
		if (failures == null || failures.isEmpty()){
			return false;
		}
		return true;
	}

	public void addFailure(Failure f){
		if (failures == null){
			failures = new ArrayList<>();
		}
		failures.add(f);
	}
}
