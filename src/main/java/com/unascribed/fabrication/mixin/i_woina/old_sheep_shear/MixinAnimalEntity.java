package com.unascribed.fabrication.mixin.i_woina.old_sheep_shear;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(AnimalEntity.class)
@EligibleIf(configAvailable="*.old_sheep_shear")
public abstract class MixinAnimalEntity extends PassiveEntity {

	protected MixinAnimalEntity(EntityType<? extends PassiveEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
	public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		Object self = this;
		if (self instanceof SheepEntity && MixinConfigPlugin.isEnabled("*.old_sheep_shear") && ((SheepEntity)self).isShearable() && source.getAttacker() instanceof PlayerEntity) {
			((SheepEntity)self).setSheared(true);
			ItemStack wool = new ItemStack(((AccessorSheepEntity)this).fabrication$getDrops().get(((SheepEntity)self).getColor()), 1 + this.random.nextInt(3));
			ItemEntity itemEntity = this.dropStack(wool, 1);
			if (itemEntity != null) {
				itemEntity.setVelocity(itemEntity.getVelocity().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1F, this.random.nextFloat() * 0.05F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
			}
		}
	}

}