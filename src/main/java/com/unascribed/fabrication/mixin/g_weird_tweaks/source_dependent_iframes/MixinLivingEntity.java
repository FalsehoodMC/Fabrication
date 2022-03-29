package com.unascribed.fabrication.mixin.g_weird_tweaks.source_dependent_iframes;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.TickSourceIFrames;
import com.unascribed.fabrication.support.ConfigPredicates;
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

import java.util.Iterator;
import java.util.LinkedHashMap;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.source_dependent_iframes")
public abstract class MixinLivingEntity extends Entity implements TickSourceIFrames {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private final LinkedHashMap<String, Integer> fabrication$iframeTracker = new LinkedHashMap<>();
	private int fabrication$timeUntilRegen = 0;

	@Inject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
	private void checkDependentIFrames(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!(FabConf.isEnabled("*.source_dependent_iframes") && ConfigPredicates.shouldRun("*.source_dependent_iframes", (LivingEntity)(Object)this))) return;
		String origin = source.getName() + (source.getAttacker() == null || source.getAttacker().getUuid() == null ? ":direct" :  source.getAttacker().getUuid().toString());
		if (fabrication$iframeTracker.containsKey(origin)) {
			this.timeUntilRegen = 20;
		} else {
			fabrication$iframeTracker.put(origin, age+9);
			this.timeUntilRegen = 0;
		}
	}
	@Inject(at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
	private void setSourceDependentIFrames(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!(FabConf.isEnabled("*.source_dependent_iframes") && ConfigPredicates.shouldRun("*.source_dependent_iframes", (LivingEntity)(Object)this))) return;
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
			Iterator<Integer> iter = fabrication$iframeTracker.values().iterator();
			iter.next();
			iter.remove();
			while (iter.hasNext()) {
				int t = iter.next()-age;
				if (t > 0) {
					fabrication$timeUntilRegen = t;
				} else {
					iter.remove();
				}
			}
		}
	}

}
