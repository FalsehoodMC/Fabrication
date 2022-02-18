package com.unascribed.fabrication.mixin.d_minor_mechanics.mechanism_muffling;

import com.unascribed.fabrication.logic.MechanismMuffling;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.support.injection.Hijack;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PistonBlock.class)
@EligibleIf(configAvailable="*.mechanism_muffling")
public class MixinPistonBlock {

	@Hijack(target={"Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V", "Lnet/minecraft/class_1937;method_8396(Lnet/minecraft/class_1657;Lnet/minecraft/class_2338;Lnet/minecraft/class_3414;Lnet/minecraft/class_3419;FF)V"},
			method={"onSyncedBlockEvent(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;II)Z", "method_9592(Lnet/minecraft/class_2680;Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;II)Z"})
	private static boolean fabrication$muffleSound(World subject, Object player, BlockPos pos) {
		return MixinConfigPlugin.isEnabled("*.mechanism_muffling") && MechanismMuffling.isMuffled(subject, pos);
	}

}
