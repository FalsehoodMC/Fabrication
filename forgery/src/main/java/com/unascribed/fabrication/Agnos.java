package com.unascribed.fabrication;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.support.Env;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.modlauncher.TransformingClassLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

// Forge implementation of Agnos. For linguistic and philosophical waffling, see the Fabric version.
public final class Agnos {

	public interface CommandRegistrationCallback {
		void register(CommandDispatcher<CommandSourceStack> commandDispatcher, boolean dedicated);
	}

	public interface TooltipRenderCallback {
		void render(ItemStack stack, List<Component> lines);
	}

	public interface HudRenderCallback {
		void render(PoseStack matrixStack, float tickDelta);
	}

	public static void runForCommandRegistration(CommandRegistrationCallback r) {
		MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> {
			r.register(e.getDispatcher(), e.getEnvironment() == CommandSelection.DEDICATED);
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

	@OnlyIn(Dist.CLIENT)
	public static KeyMapping registerKeyBinding(KeyMapping kb) {
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
