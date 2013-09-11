package org.capybara.mplayerosc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCReceiver;
import de.sciss.net.OSCServer;

public class MplayerMessageListener implements OSCListener {

	private final Logger log = LoggerFactory.getLogger(MplayerMessageListener.class);
	private int oscReplyPort;
	private MplayerManager mplayer;
	private VideoMap vidMap;
	private OSCServer c;

	public MplayerMessageListener(OSCServer c, int oscReplyPort, MplayerManager mplayer, VideoMap vidMap) {
		this.c = c;
		this.oscReplyPort = oscReplyPort;
		this.mplayer = mplayer;
		this.vidMap = vidMap;
	}

	@Override
	public void messageReceived(OSCMessage m, SocketAddress sender, long time) {
		try {
			if (((Number) m.getArg(0)).doubleValue() == 1.0) {
				log.debug("button down: "+m.getName());
				InetSocketAddress rxAddr = (InetSocketAddress) sender;
				InetSocketAddress txAddr = new InetSocketAddress(rxAddr.getAddress(), oscReplyPort);
				if (m.getName().startsWith("/vid/button_")) {
					String[] split = m.getName().split("_");
					int index = Integer.parseInt(split[1]);
					log.debug("would play video: "+index+" "+vidMap.getMap().get(index).getPath());
					mplayer.sendCommand("loadfile \""+vidMap.getMap().get(index).getPath().toAbsolutePath()+"\"");
					
				} else if ("/refresh".equals(m.getName())) {
					updateButtons(txAddr);
				}
			}
			Thread.sleep(100);			

		} catch(Exception e) {
			log.error("error handling message: "+e.getMessage(),e);
		}
	}
	
	public void updateButtons(InetSocketAddress txAddr) throws IOException {
		vidMap.updateMap();
		Map<Integer, Video> map = vidMap.getMap();
		for (int index : map.keySet()) {
			sendMessage("/vid/label_"+index,map.get(index).getName(), txAddr);
		}
	}
	
	public void sendMessage(String message, Object value, SocketAddress addr) throws IOException {
		Object[] args = { value };
		c.send(new OSCMessage(message,args),addr);
		log.debug("sent osc message: "+message+" value: "+value);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
