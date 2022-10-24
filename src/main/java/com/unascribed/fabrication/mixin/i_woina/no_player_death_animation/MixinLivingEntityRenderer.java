package com.unascribed.fabrication.mixin.i_woina.no_player_death_animation;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.ModifyGetField;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntityRenderer.class)
@EligibleIf(configAvailable="*.no_player_death_animation", envMatches=Env.CLIENT)
public abstract class MixinLivingEntityRenderer {

	@ModifyGetField(method="setupTransforms(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", target="net/minecraft/entity/LivingEntity.deathTime:I")
	private static int fabrication$oldPlayerDeathTime(int old, LivingEntity instance){
		if (FabConf.isEnabled("*.no_player_death_animation") && instance instanceof PlayerEntity) return 0;
		return old;
	}

}
