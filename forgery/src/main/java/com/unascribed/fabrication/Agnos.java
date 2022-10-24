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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.support.Env;

import cpw.mods.modlauncher.TransformingClassLoader;
import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands.EnvironmentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

// Forge implementation of Agnos. For linguistic and philosophical waffling, see the Fabric version.
public final class Agnos {

	public interface CommandRegistrationCallback {
		void register(CommandDispatcher<CommandSource> dispatcher, boolean dedicated);
	}

	public interface TooltipRenderCallback {
		void render(ItemStack stack, List<ITextComponent> lines);
	}

	public interface HudRenderCallback {
		void render(MatrixStack matrixStack, float tickDelta);
	}

	public static void runForCommandRegistration(CommandRegistrationCallback r) {
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> {
			r.register(e.getDispatcher(), e.getEnvironment() == EnvironmentType.DEDICATED);
		});
	}

	@OnlyIn(Dist.CLIENT)
	public static void runForTooltipRender(TooltipRenderCallback r) {
		MinecraftForge.EVENT_BUS.addListener((ItemTooltipEvent e) -> {
			r.render(e.getItemStack(), e.getToolTip());
		});
	}

	@OnlyIn(Dist.CLIENT)
	public static void runForHudRender(HudRenderCallback r) {
		MinecraftForge.EVENT_BUS.addListener((RenderGameOverlayEvent.Post e) -> {
			if (e.getType() == ElementType.ALL) {
				r.render(e.getMatrixStack(), e.getPartialTicks());
			}
		});
	}

	public static SoundEvent registerSoundEvent(ResourceLocation id, SoundEvent soundEvent) {
		FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(SoundEvent.class, (RegistryEvent.Register<SoundEvent> e) -> {
			soundEvent.setRegistryName(id);
			e.getRegistry().register(soundEvent);
		});
		return soundEvent;
	}

	public static ITag<Block> registerBlockTag(ResourceLocation id) {
		return ForgeTagHandler.createOptionalTag(ForgeRegistries.BLOCKS, id);
	}

	public static ITag<Item> registerItemTag(ResourceLocation id) {
		return ForgeTagHandler.createOptionalTag(ForgeRegistries.ITEMS, id);
	}

	@OnlyIn(Dist.CLIENT)
	public static KeyBinding registerKeyBinding(KeyBinding kb) {
		ClientRegistry.registerKeyBinding(kb);
		return kb;
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
		if (modid.startsWith("fabric:")) return false;
		if (modid.startsWith("forge:")) modid = modid.substring(6);
		if (ModList.get() != null) return ModList.get().isLoaded(modid);
		return FMLLoader.getLoadingModList().getModFileById(modid) != null;
	}

	public static String getModVersion() {
		return ModList.get().getModContainerById("fabrication").get().getModInfo().getVersion().toString();
	}

	public static String getLoaderVersion() {
		return ForgeVersion.getVersion();
	}

}
