package org.capybara.mplayerosc;

import java.nio.file.Path;

public class Video {

	private String name;
	private Path path;
	private int index;
	
	public Video(String name, Path path, int index) {
		this.name = name;
		this.path = path;
		this.index = index;
		
	}

	public String getName() {
		return name;
	}


	public Path getPath() {
		return path;
	}


	public int getIndex() {
		return index;
	}

}
