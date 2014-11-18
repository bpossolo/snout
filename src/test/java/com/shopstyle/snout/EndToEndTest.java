package com.shopstyle.snout;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;

public class EndToEndTest {

	@Test
	public void go() throws Exception {
		File configFile = Paths.get(EndToEndTest.class.getResource("/config.json").toURI()).toFile();
		File testsFile = Paths.get(EndToEndTest.class.getResource("/tests.json").toURI()).toFile();
		Configuration config = new Configuration(configFile, testsFile);
		TestOrchestrator orchestrator = new TestOrchestrator(config);
		orchestrator.go();
	}

}
