package com.unascribed.fabrication;

import java.nio.file.Path;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.support.Env;

import net.minecraft.item.ItemStack;

/**
 * "Agnos" can mean many things. It's a given name, a shortening of "agnostic", Aromanian for "disgust",
 * Latin for "lamb" (especially of the sacrificial variety), and Tagalog for "holy relic" (but it is
 * borrowed from a Latin phrase referring to the Latin meaning).
 * <p>
 * Agnos is all of these things. It's a name for an interface (all interfaces need names), an
 * abstraction for <i>loader agnosticism</i>, disgusting (I don't like Forge), and its FabricAgnos
 * implementer is a sacrificial lamb, replaced with ForgeAgnos by a patcher.
 */
public interface Agnos {

	// Forgery transforms this to construct its ForgeAgnos class and deletes FabricAgnos
	Agnos INST = new FabricAgnos();
	
	public interface CommandRegistrationCallback {
		void register(CommandDispatcher dispatcher, boolean dedicated);
	}
	
	public interface TooltipRenderCallback {
		void render(ItemStack stack, List lines);
	}
	
	void runForCommandRegistration(CommandRegistrationCallback r);
	void runForTooltipRender(TooltipRenderCallback r);

	/**
	 * @return {@code true} if the methods like {@link #runForCommandRegistration} are available
	 */
	boolean eventsAvailable();
	Path getConfigDir();
	Env getCurrentEnv();
	boolean isModLoaded(String modid);
	<T> T registerSoundEvent(String id, T soundEvent);
	
	<T> T registerBlockTag(String id);
	<T> T registerItemTag(String id);
	
}
