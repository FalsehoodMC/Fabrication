package com.unascribed.fabrication.mixin.f_balance.block_lit_mob_spawning;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HostileEntity.class)
@EligibleIf(configAvailable="*.block_lit_mob_spawning")
public class MixinHostileEntity {
	@ModifyReturn(method="isSpawnDark(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z", target="Lnet/minecraft/world/ServerWorldAccess;getLightLevel(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/BlockPos;)I")
	private static int fabrication$oldDimMobSpawning(int old, BlockRenderView world, LightType type) {
		if (FabConf.isEnabled("*.block_lit_mob_spawning") && type == LightType.BLOCK) {
			return 0;
		}
		return old;
	}
}
