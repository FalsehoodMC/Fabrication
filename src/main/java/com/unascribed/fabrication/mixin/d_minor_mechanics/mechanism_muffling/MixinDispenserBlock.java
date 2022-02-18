package com.unascribed.fabrication.mixin.d_minor_mechanics.mechanism_muffling;

import com.unascribed.fabrication.support.injection.Hijack;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.logic.MechanismMuffling;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DropperBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin({DispenserBlock.class, DropperBlock.class})
@EligibleIf(configAvailable="*.mechanism_muffling")
public class MixinDispenserBlock {

	@Hijack(target={"Lnet/minecraft/server/world/ServerWorld;syncWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V", "Lnet/minecraft/class_3218;method_20290(ILnet/minecraft/class_2338;I)V"},
			method={"dispense(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V", "method_10012(Lnet/minecraft/class_3218;Lnet/minecraft/class_2338;)V"})
	private static boolean fabrication$preventSyncWorldEvent(ServerWorld subject, int event, BlockPos pos) {
		return event == 1001 && MixinConfigPlugin.isEnabled("*.mechanism_muffling") && MechanismMuffling.isMuffled(subject, pos);
	}

}
