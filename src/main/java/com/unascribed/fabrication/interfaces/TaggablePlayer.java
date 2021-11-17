package com.unascribed.fabrication.interfaces;

import java.util.Set;

import com.unascribed.fabrication.logic.PlayerTag;

public interface TaggablePlayer {

	Set<PlayerTag> fabrication$getTags();
	void fabrication$clearTags();
	void fabrication$setTag(PlayerTag tag, boolean enabled);
	boolean fabrication$hasTag(PlayerTag tag);

}
