package org.capybara.mplayerosc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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
			throw new IOException("Unable to load properties from classpath: /mplayerosc.properties");
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
		Path videoPath = Paths.get(p.getProperty("video.path"));

		c = OSCServer.newUsing( OSCServer.UDP, oscPort);

		MplayerManager mplayer = new MplayerManager(mplayerPath);
		mplayer.start();

		VideoMap vidMap = new VideoMap(videoPath);
		vidMap.updateMap();

		c.addOSCListener(new MplayerMessageListener(c,oscReplyPort,mplayer, vidMap));

		c.start();

		log.info("ready & running.");
		while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				break;
			}   	 
		}
		c.stop();
	}
}
