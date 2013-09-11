package org.capybara.mplayerosc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MplayerManager {

	private final Logger log = LoggerFactory.getLogger(MplayerManager.class);
	
	private Process mplayerProcess;
	private PrintStream mplayerCommand;
	private BufferedReader mplayerReader;

	private String mplayerPath;
	
	public MplayerManager(String mplayerPath) {
		this.mplayerPath = mplayerPath;
	}
	
	public void start() throws IOException {
		ProcessBuilder pb = new ProcessBuilder(mplayerPath,"-slave","-quiet","-idle");
		Map<String,String> env = pb.environment();
		env.put("DISPLAY","localhost:0");
		mplayerProcess = pb.start();
		mplayerCommand =  new PrintStream(mplayerProcess.getOutputStream());
		mplayerReader = new BufferedReader(new InputStreamReader(mplayerProcess.getInputStream()));
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
		}
	}
}
