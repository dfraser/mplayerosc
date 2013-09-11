package org.capybara.mplayerosc;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoMap {

	private final Logger log = LoggerFactory.getLogger(VideoMap.class);
	private Path path;
	
	private Map<Integer,Video> videoList = new HashMap<>();
	
	public VideoMap(Path path) {
		this.path = path;
	}

	public void updateMap() throws IOException {
		videoList.clear();
		int i = 1;
		try (DirectoryStream<Path> stream =
		     Files.newDirectoryStream(path, "*.{avi,jpg,png,mov}")) {
		    for (Path entry: stream) {
		    	String videoName = FilenameUtils.removeExtension(entry.getFileName().toString());
		    	videoList.put(i, new Video(videoName, 	entry, i));
		    	i++;
		    }
		} 
		
	}
	
	public Map<Integer,Video> getMap() {
		return Collections.unmodifiableMap(videoList);
	}
	
}
