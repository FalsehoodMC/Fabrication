package com.unascribed.fabrication.mixin.g_weird_tweaks.source_dependent_iframes;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.TickSourceIFrames;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.source_dependent_iframes")
public abstract class MixinLivingEntity extends Entity implements TickSourceIFrames {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	Set<String> fabrication$iframeTracker = new HashSet<>();
	int fabrication$timeUntilRegen = 0;

	@Inject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
	private void checkDependentIFrames(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.source_dependent_iframes")) return;
		if (fabrication$iframeTracker.add(source.getName() + (source.getAttacker() == null || source.getAttacker().getUuid() == null ? ":direct" :  source.getAttacker().getUuid().toString()))) {
			this.timeUntilRegen = 0;
		}else {
			this.timeUntilRegen = 20;
		}
	}
	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
	private void setSourceDependentIFrames(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!FabConf.isEnabled("*.source_dependent_iframes")) return;
		if (fabrication$timeUntilRegen == 0){
			fabrication$timeUntilRegen = 10;
		}
	}
	@Inject(at=@At("HEAD"), method="baseTick()V")
	private void tickSourceDependentIFrames(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.source_dependent_iframes") || ((Object)this) instanceof ServerPlayerEntity) return;
		fabrication$tickSourceDependentIFrames();
	}
	@Override
	public void fabrication$tickSourceDependentIFrames(){
		if (fabrication$timeUntilRegen>0) {
			fabrication$timeUntilRegen--;
		}else if (!fabrication$iframeTracker.isEmpty()){
			fabrication$iframeTracker.clear();
		}
	}

}
