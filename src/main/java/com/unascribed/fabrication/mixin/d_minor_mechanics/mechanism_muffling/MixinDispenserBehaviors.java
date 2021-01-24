package com.unascribed.fabrication.mixin.d_minor_mechanics.mechanism_muffling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.MechanismMuffling;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.dispenser.BoatDispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.util.math.BlockPointer;

@Mixin({ItemDispenserBehavior.class, FallibleItemDispenserBehavior.class, ProjectileDispenserBehavior.class, BoatDispenserBehavior.class})
@EligibleIf(configEnabled="*.mechanism_muffling")
public class MixinDispenserBehaviors {

	@Inject(at=@At("HEAD"), method="playSound(Lnet/minecraft/util/math/BlockPointer;)V", cancellable=true)
	public void playSound(BlockPointer ptr, CallbackInfo ci) {
		if (MixinConfigPlugin.isEnabled("*.mechanism_muffling") && MechanismMuffling.isMuffled(ptr.getWorld(), ptr.getBlockPos())) {
			ci.cancel();
		}
	}
	
}
