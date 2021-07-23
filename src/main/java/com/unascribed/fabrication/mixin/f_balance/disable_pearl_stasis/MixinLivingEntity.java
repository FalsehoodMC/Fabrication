package com.unascribed.fabrication.mixin.f_balance.disable_pearl_stasis;


import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownItemEntity.class)
@EligibleIf(configEnabled="*.disable_pearl_stasis")
public abstract class MixinLivingEntity  {
    @Inject(at=@At("TAIL"), method= "writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V")
    public void remove_pearl_owner(CompoundTag tag, CallbackInfo ci) {
        if(((Object)this) instanceof EnderPearlEntity && MixinConfigPlugin.isEnabled("*.disable_pearl_stasis"))
            tag.remove("Owner");
    }
}