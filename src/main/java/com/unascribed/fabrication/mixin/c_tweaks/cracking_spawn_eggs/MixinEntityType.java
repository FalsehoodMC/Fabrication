package com.unascribed.fabrication.mixin.c_tweaks.cracking_spawn_eggs;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Mixin(EntityType.class)
@EligibleIf(configAvailable="*.cracking_spawn_eggs")
public class MixinEntityType {

	@FabInject(at=@At("RETURN"), method="spawnFromItemStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;ZZ)Lnet/minecraft/entity/Entity;")
	public void spawnFromItemStack(ServerWorld world, ItemStack stack, PlayerEntity player, BlockPos pos, SpawnReason reason, boolean alignPosition, boolean invertY, CallbackInfoReturnable<Entity> ci) {
		if (!FabConf.isEnabled("*.cracking_spawn_eggs") || stack == null) return;
		if (stack.getItem() instanceof SpawnEggItem) {
			Entity e = ci.getReturnValue();
			Box box = e.getBoundingBox();
			Vec3d center = box.getCenter();
			world.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), center.x, center.y, center.z, 20, box.getXLength()/2, box.getYLength()/2, box.getZLength()/2, 0.05);
			e.playSound(SoundEvents.ENTITY_TURTLE_EGG_CRACK, 1.0f, 1.0f);
		}
	}

}
