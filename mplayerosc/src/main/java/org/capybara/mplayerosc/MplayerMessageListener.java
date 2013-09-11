package org.capybara.mplayerosc;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;

public class MplayerMessageListener implements OSCListener {

	private final Logger log = LoggerFactory.getLogger(MplayerMessageListener.class);
	private int oscReplyPort;
	private MplayerManager mplayer;
	
	public MplayerMessageListener(int oscReplyPort, MplayerManager mplayer) {
		this.oscReplyPort = oscReplyPort;
		this.mplayer = mplayer;
	}

	@Override
	public void messageReceived(OSCMessage m, SocketAddress sender, long time) {
		log.debug("message rx: "+m);
		if (m.getArgCount() > 0) {
			if (m.getArg(0) instanceof Float) {
				try {
					Double value = new Double((Float)m.getArg(0));
					if (value != 1.0) {
						log.debug("skipping non-1.0 message");
						return;
					}
					
					InetSocketAddress rxAddr = (InetSocketAddress) sender;
					InetSocketAddress txAddr = new InetSocketAddress(rxAddr.getAddress(), oscReplyPort);
					Thread.sleep(100);			
					
				} catch(Exception e) {
					log.error("error handling message: "+e.getMessage(),e);
				}
			}
		}
	}
}
