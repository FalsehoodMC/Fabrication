package com.unascribed.fabrication.mixin.i_woina.old_sheep_shear;

import com.unascribed.fabrication.features.FeatureOldLava;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteAtlasTexture.Data;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Arm;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SheepEntity.class)
@EligibleIf(configEnabled="*.old_sheep_shear")
public abstract class MixinSheepEntity extends AnimalEntity {

	@Final
	@Shadow
	private static Map<DyeColor, ItemConvertible> DROPS;
	@Shadow 
	public abstract boolean isShearable();
	@Shadow
	public abstract void setSheared(boolean sheared);
	@Shadow
	public abstract DyeColor getColor();

	protected MixinSheepEntity(EntityType<? extends AnimalEntity> entityType, World world) {
		super(entityType, world);
	}

	public boolean damage(DamageSource source, float amount) {
		if (MixinConfigPlugin.isEnabled("*.old_sheep_shear") && this.isShearable() && source.getAttacker() instanceof PlayerEntity) {
			this.setSheared(true);
			ItemStack wool = DROPS.get(this.getColor()).asItem().getDefaultStack();
			wool.setCount(1 + this.random.nextInt(3));
			ItemEntity itemEntity = this.dropStack(wool, 1);
			if (itemEntity != null) {
				itemEntity.setVelocity(itemEntity.getVelocity().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1F, this.random.nextFloat() * 0.05F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
			}
		}
		return super.damage(source, amount);
	}

	@Surrogate
	public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) { return null; }
}