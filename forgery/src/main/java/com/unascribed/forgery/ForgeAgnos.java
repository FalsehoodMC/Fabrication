package com.unascribed.forgery;

import java.nio.file.Path;

import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.support.Env;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeAgnos implements Agnos {

	@Override
	public void runForCommandRegistration(CommandRegistrationCallback r) {
		MinecraftForge.EVENT_BUS.addListener((FMLServerAboutToStartEvent e) -> {
			r.register(e.getServer().getCommandManager().getDispatcher(), e.getServer().isDedicatedServer());
		});
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void runForTooltipRender(TooltipRenderCallback r) {
		MinecraftForge.EVENT_BUS.addListener((ItemTooltipEvent e) -> {
			r.render(e.getItemStack(), e.getToolTip());
		});
	}
	
	@Override
	public <T> T registerSoundEvent(String id, T soundEvent) {
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(SoundEvent.class, (RegistryEvent.Register<SoundEvent> e) -> {
			((SoundEvent)soundEvent).setRegistryName(id);
			e.getRegistry().register((SoundEvent)soundEvent);
		});
		return soundEvent;
	}
	
	@Override
	public <T> T registerBlockTag(String id) {
		return (T)ForgeTagHandler.createOptionalTag(ForgeRegistries.BLOCKS, new ResourceLocation(id));
	}

	@Override
	public <T> T registerItemTag(String id) {
		return (T)ForgeTagHandler.createOptionalTag(ForgeRegistries.ITEMS, new ResourceLocation(id));
	}

	@Override
	public boolean eventsAvailable() {
		return true;
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public Env getCurrentEnv() {
		return FMLEnvironment.dist == Dist.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	@Override
	public boolean isModLoaded(String modid) {
		return false;
	}
	
	@Override
	public String getModVersion() {
		return ModList.get().getModContainerById("fabrication").get().getModInfo().getVersion().toString();
	}

}
