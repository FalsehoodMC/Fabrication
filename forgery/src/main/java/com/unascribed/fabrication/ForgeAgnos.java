package com.unascribed.fabrication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;

import com.unascribed.fabrication.support.Env;

import cpw.mods.modlauncher.TransformingClassLoader;
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
import net.minecraftforge.fml.loading.FMLLoader;
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
		return FMLLoader.getLoadingModList().getModFileById(modid) != null;
	}
	
	@Override
	public String getModVersion() {
		return ModList.get().getModContainerById("fabrication").get().getModInfo().getVersion().toString();
	}
	
	@Override
	public byte[] getClassBytes(Class<?> clazz) {
		try {
			// Forge why are you like this
			TransformingClassLoader tcl = FMLLoader.getLaunchClassLoader();
			Field finderF = TransformingClassLoader.class.getDeclaredField("resourceFinder");
			finderF.setAccessible(true);
			Function<String,Enumeration<URL>> finder = (Function<String, Enumeration<URL>>)finderF.get(tcl);
			Field dclF = TransformingClassLoader.class.getDeclaredField("delegatedClassLoader");
			dclF.setAccessible(true);
			Object dcl = dclF.get(tcl);
			Method m = dcl.getClass().getDeclaredMethod("findClass", String.class, Function.class, String.class);
			m.setAccessible(true);
			Map.Entry<byte[], CodeSource> en = (Entry<byte[], CodeSource>)m.invoke(dcl, clazz.getName(), finder, "Forgery class lookup");
			if (en == null) return null;
			return en.getKey();
		} catch (Throwable t) {
			LogManager.getLogger("Fabrication").warn("Failed to look up "+clazz, t);
			return null;
		}
	}

}
