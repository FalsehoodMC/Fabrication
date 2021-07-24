package com.unascribed.fabrication.mixin.a_fixes.sync_attacker_yaw;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.interfaces.SetAttackerYawAware;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@EligibleIf(configEnabled="*.sync_attacker_yaw")
public abstract class MixinLivingEntity extends Entity implements SetAttackerYawAware {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private static final Identifier FABRICATION$ATTACKER_YAW = new Identifier("fabrication", "attacker_yaw");
		
	private float fabrication$lastAttackerYaw;
	private boolean fabrication$attackerYawAware;
	
	// actually attackerYaw. has the wrong name in this version of yarn
	@Shadow
	private float knockbackVelocity;
	
	@Inject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
	public void damageHead(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!MixinConfigPlugin.isEnabled("*.sync_attacker_yaw")) return;
		fabrication$lastAttackerYaw = knockbackVelocity;
	}
	
	@Inject(at=@At("RETURN"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
	public void damageReturn(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!MixinConfigPlugin.isEnabled("*.sync_attacker_yaw")) return;
		if (source == DamageSource.OUT_OF_WORLD && MixinConfigPlugin.isEnabled("*.repelling_void")) {
			knockbackVelocity = getYaw();
		}
		Object self = this;
		if (self instanceof PlayerEntity && knockbackVelocity != fabrication$lastAttackerYaw && world != null && !world.isClient) {
			PacketByteBuf data = new PacketByteBuf(Unpooled.buffer(8));
			data.writeInt(getId());
			data.writeFloat(knockbackVelocity);
			FabricationMod.sendToTrackersMatching(this, new CustomPayloadS2CPacket(FABRICATION$ATTACKER_YAW, data), spe -> spe instanceof SetAttackerYawAware && ((SetAttackerYawAware) spe).fabrication$isAttackerYawAware());
		}
	}
	
	@Override
	public boolean fabrication$isAttackerYawAware() {
		return fabrication$attackerYawAware;
	}
	
	@Override
	public void fabrication$setAttackerYawAware(boolean aware) {
		fabrication$attackerYawAware = aware;
	}
	
}
