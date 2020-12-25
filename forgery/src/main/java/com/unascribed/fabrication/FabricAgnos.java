package com.unascribed.fabrication;

import java.nio.file.Path;

import com.unascribed.fabrication.support.Env;

public class FabricAgnos implements Agnos {

	@Override
	public void runForCommandRegistration(CommandRegistrationCallback r) {
		throw new IllegalArgumentException("Stub!");
	}

	@Override
	public boolean eventsAvailable() {
		throw new IllegalArgumentException("Stub!");
	}

	@Override
	public Path getConfigDir() {
		throw new IllegalArgumentException("Stub!");
	}

	@Override
	public Env getCurrentEnv() {
		throw new IllegalArgumentException("Stub!");
	}

	@Override
	public boolean isModLoaded(String modid) {
		throw new IllegalArgumentException("Stub!");
	}

	@Override
	public <T> T registerSoundEvent(String id, T soundEvent) {
		throw new IllegalArgumentException("Stub!");
	}

	@Override
	public void runForTooltipRender(TooltipRenderCallback r) {
		throw new IllegalArgumentException("Stub!");
	}

	@Override
	public <T> T registerBlockTag(String id) {
		throw new IllegalArgumentException("Stub!");
	}

	@Override
	public <T> T registerItemTag(String id) {
		throw new IllegalArgumentException("Stub!");
	}
	
	@Override
	public String getModVersion() {
		throw new IllegalArgumentException("Stub!");
	}

}
