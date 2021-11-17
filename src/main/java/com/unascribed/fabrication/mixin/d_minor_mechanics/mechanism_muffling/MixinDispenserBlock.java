package com.unascribed.fabrication.mixin.d_minor_mechanics.mechanism_muffling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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

	@Redirect(at=@At(value="INVOKE", target="net/minecraft/server/world/ServerWorld.syncWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V"),
			method="dispense(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V")
	public void syncWorldEvent(ServerWorld subject, int event, BlockPos pos, int data) {
		if (event == 1001 && MixinConfigPlugin.isEnabled("*.mechanism_muffling") && MechanismMuffling.isMuffled(subject, pos)) return;
		subject.syncWorldEvent(event, pos, data);
	}

}
