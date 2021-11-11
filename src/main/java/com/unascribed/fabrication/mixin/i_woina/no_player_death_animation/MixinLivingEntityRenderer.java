package com.unascribed.fabrication.mixin.i_woina.no_player_death_animation;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
@EligibleIf(configAvailable="*.no_player_death_animation")
abstract public class MixinLivingEntityRenderer {

    @Redirect(method="setupTransforms(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/util/math/MatrixStack;FFF)V", at=@At(value="FIELD", target="Lnet/minecraft/entity/LivingEntity;deathTime:I"))
    public int oldDeath(LivingEntity instance){
        if (MixinConfigPlugin.isEnabled("*.no_player_death_animation") && instance instanceof PlayerEntity) return 0;
        return instance.deathTime;
    }

}
