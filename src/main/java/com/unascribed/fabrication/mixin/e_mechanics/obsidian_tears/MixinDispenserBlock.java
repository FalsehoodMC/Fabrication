package com.unascribed.fabrication.mixin.e_mechanics.obsidian_tears;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.logic.ObsidianTears;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Mixin(DispenserBlock.class)
@EligibleIf(configEnabled="*.obsidian_tears")
public class MixinDispenserBlock {

	@Inject(at=@At("HEAD"), method="getBehaviorForItem(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/block/dispenser/DispenserBehavior;",
			cancellable=true)
	public void getBehaviorForItem(ItemStack stack, CallbackInfoReturnable<DispenserBehavior> ci) {
		if (!MixinConfigPlugin.isEnabled("*.obsidian_tears")) return;
		if (stack.getItem() == Items.POTION && stack.hasTag() && stack.getTag().getBoolean("fabrication:ObsidianTears")) {
			ci.setReturnValue(ObsidianTears.DISPENSER_BEHAVIOR);
		}
	}
	
}
