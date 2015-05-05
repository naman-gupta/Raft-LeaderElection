package com.raft;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;

public class Main {

	static RaftServerPool sp;

	private static Properties props = new Properties();

	public static void main(String[] args) throws IOException {

		// loading properties file having cluster information.
		props.load(new FileInputStream("config.properties"));

		Timer timer = new Timer();
		
		int caseNumber =  6 ;
		
		// create the server pool
		sp = new RaftServerPool(props,caseNumber);

		// do maintenance processes every 1sec
		timer.schedule(sp, 500, 1000);

		// start the pool and print status
		sp.start();

	}

}
