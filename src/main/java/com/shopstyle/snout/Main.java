package com.shopstyle.snout;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

	private static final Logger log = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		if (args.length != 2){
			System.out.println("Usage: snout <config-filename> <tests-filename>");
			System.exit(1);
		}
		File configFile = new File(args[0]);
		File testsFile = new File(args[1]);
		if (!configFile.exists()){
			System.out.println("Config file [" + configFile.getAbsolutePath() + "] does not exist");
			System.exit(2);
		}
		if (!testsFile.exists()){
			System.out.println("Tests file [" + testsFile.getAbsolutePath() + "] does not exist");
			System.exit(2);
		}
		try{
			Configuration config = new Configuration(configFile, testsFile);
			TestOrchestrator orchestrator = new TestOrchestrator(config);
			orchestrator.go();
		}
		catch(Exception e){
			log.log(Level.SEVERE, "Failure...", e);
		}
	}

}
