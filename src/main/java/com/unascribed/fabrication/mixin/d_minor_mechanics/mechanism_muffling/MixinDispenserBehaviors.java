package com.unascribed.fabrication.mixin.d_minor_mechanics.mechanism_muffling;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.logic.MechanismMuffling;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.dispenser.BoatDispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.util.math.BlockPointer;

@Mixin({ItemDispenserBehavior.class, FallibleItemDispenserBehavior.class, ProjectileDispenserBehavior.class, BoatDispenserBehavior.class})
@EligibleIf(configAvailable="*.mechanism_muffling")
public class MixinDispenserBehaviors {

	@FabInject(at=@At("HEAD"), method="playSound(Lnet/minecraft/util/math/BlockPointer;)V", cancellable=true)
	public void playSound(BlockPointer ptr, CallbackInfo ci) {
		if (FabConf.isEnabled("*.mechanism_muffling") && MechanismMuffling.isMuffled(ptr.getWorld(), ptr.getPos())) {
			ci.cancel();
		}
	}

}
