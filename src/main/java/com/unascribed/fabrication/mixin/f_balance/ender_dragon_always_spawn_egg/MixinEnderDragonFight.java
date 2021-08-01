package com.unascribed.fabrication.mixin.f_balance.ender_dragon_always_spawn_egg;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.EndPortalFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(EnderDragonFight.class)
@EligibleIf(configAvailable="*.ender_dragon_always_spawn_egg")
public class MixinEnderDragonFight {
	@Shadow
	private boolean previouslyKilled;
	@Shadow
	private UUID dragonUuid;
	@Shadow
	private ServerWorld world;

	@Inject(method = "dragonKilled(Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;)V", at = @At(value = "TAIL"))
	public void dragonKilled(EnderDragonEntity dragon, CallbackInfo ci){
		if (MixinConfigPlugin.isEnabled("*.ender_dragon_always_spawn_egg") && previouslyKilled && dragon.getUuid().equals(this.dragonUuid)){
			this.world.setBlockState(this.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, EndPortalFeature.ORIGIN), Blocks.DRAGON_EGG.getDefaultState());
		}
	}
}
