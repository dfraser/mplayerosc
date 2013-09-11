package org.capybara.mplayerosc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MplayerManager {

	private final Logger log = LoggerFactory.getLogger(MplayerManager.class);
	
	private Process mplayerProcess;
	private PrintStream mplayerCommand;
	private BufferedReader mplayerReader;

	private String mplayerPath;

	private ExecutorService outputPrinter;
	
	public MplayerManager(String mplayerPath) {
		this.mplayerPath = mplayerPath;
	}
	
	public void start() throws IOException {
		ProcessBuilder pb = new ProcessBuilder(mplayerPath,"-slave","-quiet","-idle","-fs","-zoom","−fixed−vo");
		Map<String,String> env = pb.environment();
//		env.put("DISPLAY","localhost:0");
		mplayerProcess = pb.start();
		mplayerCommand =  new PrintStream(mplayerProcess.getOutputStream());
		mplayerReader = new BufferedReader(new InputStreamReader(mplayerProcess.getInputStream()));
		outputPrinter = Executors.newSingleThreadExecutor();
		outputPrinter.execute(new OutputPrinter(mplayerReader));
	}
	
	public void sendCommand(String command) {
		mplayerCommand.println(command);
		mplayerCommand.flush();
	}
	
	public void stop() {
		if (mplayerProcess != null) {
			mplayerProcess.destroy();
			try {
				mplayerProcess.wait();
			} catch (InterruptedException e) {
				log.error("interrupted while waiting for mplayer to terminate",e);
			}
			outputPrinter.shutdownNow();
		}
	}
	
	public static class OutputPrinter implements Runnable {

		private BufferedReader reader;
		
		private final Logger log = LoggerFactory.getLogger(OutputPrinter.class);
		
		public OutputPrinter(BufferedReader reader) {
			this.reader = reader;
			
		}
		@Override
		public void run() {
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					log.debug(line);
				}
			} catch(IOException e) {
				log.error("IOException reading log: "+e.getMessage(),e);
			}
		}
		
	}
}
