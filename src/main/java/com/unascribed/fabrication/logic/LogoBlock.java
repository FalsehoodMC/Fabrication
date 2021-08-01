package com.unascribed.fabrication.logic;

import java.util.concurrent.ThreadLocalRandom;

import com.unascribed.fabrication.loaders.LoaderBlockLogo;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class LogoBlock {

	public float position;
	public float lastPosition;
	public float velocity;
	public BlockState state;

	public LogoBlock(int x, int y, BlockState state) {
		position = lastPosition = (10 + y) + ThreadLocalRandom.current().nextFloat() * 32 + x;
		this.state = state;
	}

	public void tick() {
		lastPosition = position;
		if (position > 0) {
			velocity -= 0.6f;
		}
		position += velocity;
		velocity *= 0.9f;
		if (position < 0) {
			position = 0;
			velocity = 0;
			if (lastPosition > 0) {
				if (LoaderBlockLogo.sound) {
					MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(state == null ? SoundEvents.BLOCK_BAMBOO_BREAK : state.getSoundGroup().getPlaceSound(), 1f, 0.2f));
				}
			}
		}
	}

}
