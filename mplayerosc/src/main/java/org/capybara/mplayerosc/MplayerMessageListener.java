package org.capybara.mplayerosc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

public class MplayerMessageListener implements OSCListener {

	private final Logger log = LoggerFactory.getLogger(MplayerMessageListener.class);
	private int oscReplyPort;
	private MplayerManager mplayer;
	private VideoMap vidMap;
	private OSCServer c;
	private String pauseClip;
	private int pauseClipIndex;
	private boolean pauseState;
	private int blackClipIndex;

	public MplayerMessageListener(OSCServer c, int oscReplyPort, MplayerManager mplayer, VideoMap vidMap, String pauseClip) {
		this.c = c;
		this.oscReplyPort = oscReplyPort;
		this.mplayer = mplayer;
		this.vidMap = vidMap;
		this.pauseClip = pauseClip;
	
	}

	@Override
	public void messageReceived(OSCMessage m, SocketAddress sender, long time) {
		try {
			InetSocketAddress rxAddr = (InetSocketAddress) sender;
			InetSocketAddress txAddr = new InetSocketAddress(rxAddr.getAddress(), oscReplyPort);

			if ("/volume".equals(m.getName())) {
				int volLevel = (int) (((Number) m.getArg(0)).doubleValue() * 100);
				mplayer.sendCommand("volume "+volLevel+" 1");
			} else if (((Number) m.getArg(0)).doubleValue() == 1.0) {
				log.debug("button down: "+m.getName());
				if ("/refresh".equals(m.getName())) {
					updateButtons(txAddr);
				}
				else if ("/pause".equals(m.getName())) {
					pauseState = !pauseState;
					setPauseButtonState(pauseState,txAddr);
					mplayer.sendCommand("pause");
				}
				else if (m.getName().startsWith("/vid/button_")) {
					String[] split = m.getName().split("_");
					int index = 0;
					if ("pause".equals(split[1])) {
						index = pauseClipIndex;
					} else if ("black".equals(split[1])) {
						index = blackClipIndex;
					} else {
						index = Integer.parseInt(split[1]);
					}
					log.debug("would play video: "+index+" "+vidMap.getMap().get(index).getPath());
					String fileUrl = getFileUrl(index);
					mplayer.sendCommand("loadfile "+fileUrl);
					if (index != pauseClipIndex && !isImage(index)) {
						fileUrl = getFileUrl(pauseClipIndex);
						mplayer.sendCommand("loadfile "+fileUrl+" 1");
					}
					pauseState = false;
					setPauseButtonState(pauseState,txAddr);
				}
			}
//			Thread.sleep(100);			

		} catch(Exception e) {
			log.error("error handling message: "+e.getMessage(),e);
		}
	}

	private void setPauseButtonState(boolean state,InetSocketAddress txAddr) throws IOException {
		if (state == true) {
			sendMessage("/pause",new Double(1.0),txAddr);
			sendMessage("/pause/color","black",txAddr);
		} else {
			sendMessage("/pause",new Double(0.0),txAddr);
			sendMessage("/pause/color","yellow",txAddr);
			
		}
	}
	
	private String getFileUrl(int index) {
		String fileUrl;
		Path filePath = vidMap.getMap().get(index).getPath();
		if (isImage(index)) {
			fileUrl = "mf://"+filePath.toAbsolutePath().toString();
		} else {
			fileUrl = filePath.toAbsolutePath().toString();
		}
		fileUrl = "\"" + fileUrl + "\"";
		return fileUrl;
	}

	private boolean isImage(int index) {
		Path filePath = vidMap.getMap().get(index).getPath();
		return filePath.toString().endsWith(".jpg") || filePath.toString().endsWith(".png");
	}
	
	public void updateButtons(InetSocketAddress txAddr) throws IOException {
		vidMap.updateMap();
		boolean foundPauseClip = false;
		boolean foundBlackClip = false;
		Map<Integer, Video> map = vidMap.getMap();
		for (int index = 1; index < 43; index++) {
			if (map.containsKey(index)) {
				sendMessage("/vid/label_"+index+"/visible",new Double(1),txAddr);
				sendMessage("/vid/button_"+index+"/visible",new Double(1),txAddr);
				
				sendMessage("/vid/label_"+index,map.get(index).getName(), txAddr);
				if (isImage(index)) {
					sendMessage("/vid/label_"+index+"/color","green", txAddr);
					sendMessage("/vid/button_"+index+"/color","green", txAddr);
				} else {
					sendMessage("/vid/label_"+index+"/color","red", txAddr);
					sendMessage("/vid/button_"+index+"/color","red", txAddr);
					
				}
				if (map.get(index).getPath().getFileName().toString().equals(pauseClip)) {
					pauseClipIndex = index;
					sendMessage("/vid/label_pause",map.get(index).getName(), txAddr);
					log.debug("setting pause clip to "+pauseClip+" index "+index);
					foundPauseClip = true;
				}
				if (map.get(index).getPath().getFileName().toString().equals("black.jpg")) {
					blackClipIndex = index;
					sendMessage("/vid/label_black",map.get(index).getName(), txAddr);
					log.debug("setting black clip to black.jpg index "+index);
					foundBlackClip = true;
				}
			} else {
				sendMessage("/vid/label_"+index+"/visible",new Double(0),txAddr);
				sendMessage("/vid/button_"+index+"/visible",new Double(0),txAddr);
			}
		}
		if (foundPauseClip == false) {
			log.warn("unable to find pause clip matching "+pauseClip);
		}
		if (foundBlackClip == false) {
			log.warn("unable to find black clip matching black.jpg");
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
