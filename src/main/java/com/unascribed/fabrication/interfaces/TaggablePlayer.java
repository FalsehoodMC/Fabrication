package com.unascribed.fabrication.interfaces;

import java.util.Set;

public interface TaggablePlayer {

	Set<String> fabrication$getTags();
	void fabrication$clearTags();
	void fabrication$setTag(String tag, boolean enabled);
	boolean fabrication$hasTag(String tag);

}
