package com.unascribed.fabrication;

import java.util.concurrent.ThreadLocalRandom;

import com.unascribed.fabrication.loaders.LoaderBlockLogo;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;

public class LogoBlock {

	public float progress;
	public float lastProgress;
	public float velocity;
	public BlockState state;

	public LogoBlock(int x, int y, BlockState state) {
		progress = lastProgress = (10 + y) + ThreadLocalRandom.current().nextFloat() * 32 + x;
		this.state = state;
	}

	public void tick() {
		lastProgress = progress;
		if (progress > 0) {
			velocity -= 0.6f;
		}
		progress += velocity;
		velocity *= 0.9f;
		if (progress < 0) {
			progress = 0;
			velocity = 0;
			if (lastProgress > 0) {
				if (LoaderBlockLogo.sound) {
					MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(state.getSoundGroup().getPlaceSound(), 1f, 0.2f));
				}
			}
		}
	}

}
