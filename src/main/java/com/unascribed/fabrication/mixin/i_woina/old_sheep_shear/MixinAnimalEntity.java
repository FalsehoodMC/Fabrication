package com.unascribed.fabrication.mixin.i_woina.old_sheep_shear;

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
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimalEntity.class)
@EligibleIf(configEnabled="*.old_sheep_shear")
public abstract class MixinAnimalEntity extends PassiveEntity {

	protected MixinAnimalEntity(EntityType<? extends PassiveEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
	public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		Object self = this;
		if (self instanceof SheepEntity && MixinConfigPlugin.isEnabled("*.old_sheep_shear") && ((SheepEntity)self).isShearable() && source.getAttacker() instanceof PlayerEntity) {
			((SheepEntity)self).setSheared(true);
			ItemStack wool;
			switch(((SheepEntity)self).getColor()) {
				case WHITE:
				default:
					wool = Items.WHITE_WOOL.getDefaultStack();
					break;
				case ORANGE:
					wool = Items.ORANGE_WOOL.getDefaultStack();
					break;
				case MAGENTA:
					wool = Items.MAGENTA_WOOL.getDefaultStack();
					break;
				case LIGHT_BLUE:
					wool = Items.LIGHT_BLUE_WOOL.getDefaultStack();
					break;
				case YELLOW:
					wool = Items.YELLOW_WOOL.getDefaultStack();
					break;
				case LIME:
					wool = Items.LIME_WOOL.getDefaultStack();
					break;
				case PINK:
					wool = Items.PINK_WOOL.getDefaultStack();
					break;
				case GRAY:
					wool = Items.GRAY_WOOL.getDefaultStack();
					break;
				case LIGHT_GRAY:
					wool = Items.LIGHT_GRAY_WOOL.getDefaultStack();
					break;
				case CYAN:
					wool = Items.CYAN_WOOL.getDefaultStack();
					break;
				case PURPLE:
					wool = Items.PURPLE_WOOL.getDefaultStack();
					break;
				case BLUE:
					wool = Items.BLUE_WOOL.getDefaultStack();
					break;
				case BROWN:
					wool = Items.BROWN_WOOL.getDefaultStack();
					break;
				case GREEN:
					wool = Items.GREEN_WOOL.getDefaultStack();
					break;
				case RED:
					wool = Items.RED_WOOL.getDefaultStack();
					break;
				case BLACK:
					wool = Items.BLACK_WOOL.getDefaultStack();
					break;
			}
			wool.setCount(1 + this.random.nextInt(3));
			ItemEntity itemEntity = this.dropStack(wool, 1);
			if (itemEntity != null) {
				itemEntity.setVelocity(itemEntity.getVelocity().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1F, this.random.nextFloat() * 0.05F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
			}
		}
	}
	
}