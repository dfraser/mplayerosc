package org.capybara.mplayerosc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCServer;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private static final Logger log = LoggerFactory.getLogger(App.class);

	private OSCServer c;

	public Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		InputStream in = getClass().getResourceAsStream("/mplayerosc.properties");
		if (in == null) {
			throw new IOException("Unable to load properties");
		}
		properties.load(in);
		in.close();
		return properties;
	}
	
    public static void main( String[] args ) throws Exception
    {
    	App app = new App();
    	app.run();
    }

	private void run() throws IOException {
		Properties p = loadProperties();
		
		String mplayerPath = p.getProperty("mplayer.path");
		int oscPort = Integer.parseInt(p.getProperty("osc.listenPort"));
		int oscReplyPort = Integer.parseInt(p.getProperty("osc.replyPort"));
		
		c = OSCServer.newUsing( OSCServer.UDP, oscPort);
		
		MplayerManager mplayer = new MplayerManager(mplayerPath);
		mplayer.start();
		
		c.addOSCListener(new MplayerMessageListener(oscReplyPort,mplayer));
	}
}
