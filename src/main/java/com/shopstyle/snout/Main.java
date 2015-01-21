package com.shopstyle.snout;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

	private static final Logger log = Logger.getLogger(Main.class.getName());
	private static final int Success = 0;
	private static final int InsufficientArgs = 1;
	private static final int FileDoesntExist = 2;
	private static final int FailedTests = 3;

	public static void main(String[] args) {
		if (args.length != 2){
			System.out.println("Usage: main.sh <config-filename> <tests-filename>");
			System.exit(InsufficientArgs);
		}
		File configFile = new File(args[0]);
		File testsFile = new File(args[1]);
		if (!configFile.exists()){
			System.out.println("Config file [" + configFile.getAbsolutePath() + "] does not exist");
			System.exit(FileDoesntExist);
		}
		if (!testsFile.exists()){
			System.out.println("Tests file [" + testsFile.getAbsolutePath() + "] does not exist");
			System.exit(FileDoesntExist);
		}
		try{
			Configuration config = new Configuration(configFile, testsFile);
			TestOrchestrator orchestrator = new TestOrchestrator(config);
			boolean success = orchestrator.go();
			if (success){
				System.exit(Success);
			}
			else {
				System.exit(FailedTests);
			}
		}
		catch(Exception e){
			log.log(Level.SEVERE, "Failure...", e);
		}
	}

}
