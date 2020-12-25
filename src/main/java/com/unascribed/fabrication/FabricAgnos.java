package com.unascribed.fabrication;

import java.nio.file.Path;

import com.unascribed.fabrication.support.Env;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FabricAgnos implements Agnos {

	@Override
	public void runForCommandRegistration(CommandRegistrationCallback r) {
		net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register(r::register);
	}
	
	@Override
	public void runForTooltipRender(TooltipRenderCallback r) {
		ItemTooltipCallback.EVENT.register((stack, ctx, lines) -> r.render(stack, lines));
	}
	
	@Override
	public <T> T registerSoundEvent(String id, T soundEvent) {
		return (T)Registry.register(Registry.SOUND_EVENT, new Identifier(id), (SoundEvent)soundEvent);
	}
	
	@Override
	public <T> T registerBlockTag(String id) {
		return (T)TagRegistry.block(new Identifier(id));
	}

	@Override
	public <T> T registerItemTag(String id) {
		return (T)TagRegistry.item(new Identifier(id));
	}
	
	@Override
	public boolean eventsAvailable() {
		return FabricLoader.getInstance().isModLoaded("fabric");
	}

	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	public Env getCurrentEnv() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	@Override
	public boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}
	
	@Override
	public String getModVersion() {
		return FabricLoader.getInstance().getModContainer("fabrication").get().getMetadata().getVersion().getFriendlyString();
	}
	
}
