package com.unascribed.fabrication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;

import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.support.Env;

import cpw.mods.modlauncher.TransformingClassLoader;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
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

// Forge implementation of Agnos. For linguistic and philosophical waffling, see the Fabric version.
public final class Agnos {

	public interface CommandRegistrationCallback {
		void register(CommandDispatcher<CommandSource> dispatcher, boolean dedicated);
	}
	
	public interface TooltipRenderCallback {
		void render(ItemStack stack, List<ITextComponent> lines);
	}
	
	public static void runForCommandRegistration(CommandRegistrationCallback r) {
		MinecraftForge.EVENT_BUS.addListener((FMLServerAboutToStartEvent e) -> {
			r.register(e.getServer().getCommandManager().getDispatcher(), e.getServer().isDedicatedServer());
		});
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void runForTooltipRender(TooltipRenderCallback r) {
		MinecraftForge.EVENT_BUS.addListener((ItemTooltipEvent e) -> {
			r.render(e.getItemStack(), e.getToolTip());
		});
	}
	
	public static <T> T registerSoundEvent(String id, T soundEvent) {
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(SoundEvent.class, (RegistryEvent.Register<SoundEvent> e) -> {
			((SoundEvent)soundEvent).setRegistryName(id);
			e.getRegistry().register((SoundEvent)soundEvent);
		});
		return soundEvent;
	}
	
	public static <T> T registerBlockTag(String id) {
		return (T)ForgeTagHandler.createOptionalTag(ForgeRegistries.BLOCKS, new ResourceLocation(id));
	}

	public static <T> T registerItemTag(String id) {
		return (T)ForgeTagHandler.createOptionalTag(ForgeRegistries.ITEMS, new ResourceLocation(id));
	}

	public static boolean eventsAvailable() {
		return true;
	}

	public static Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	public static Env getCurrentEnv() {
		return FMLEnvironment.dist == Dist.CLIENT ? Env.CLIENT : Env.SERVER;
	}

	public static boolean isModLoaded(String modid) {
		return FMLLoader.getLoadingModList().getModFileById(modid) != null;
	}
	
	public static String getModVersion() {
		return ModList.get().getModContainerById("fabrication").get().getModInfo().getVersion().toString();
	}
	
	public static byte[] getClassBytes(Class<?> clazz) {
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
