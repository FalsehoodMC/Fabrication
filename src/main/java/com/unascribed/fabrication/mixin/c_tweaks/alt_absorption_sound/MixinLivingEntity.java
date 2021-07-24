package com.unascribed.fabrication.mixin.c_tweaks.alt_absorption_sound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.interfaces.DidJustAbsorp;
import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@EligibleIf(configEnabled="*.alt_absorption_sound")
public abstract class MixinLivingEntity extends Entity implements DidJustAbsorp {
	
	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Unique
	private float fabrication$absorptionAmountBeforeDamage;
	
	@Shadow
	protected float lastDamageTaken;
	
	@Shadow
	public abstract float getAbsorptionAmount();
	@Shadow
	public abstract SoundEvent getHurtSound(DamageSource src);
	@Shadow
	protected abstract float getSoundVolume();
	@Shadow
	protected abstract float getSoundPitch();
	
	@Inject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
	public void damage(DamageSource ds, float amount, CallbackInfoReturnable<Boolean> cir) {
		fabrication$absorptionAmountBeforeDamage = getAbsorptionAmount();
	}
	
	@Override
	public boolean fabrication$didJustAbsorp() {
		return getAbsorptionAmount() < fabrication$absorptionAmountBeforeDamage && fabrication$absorptionAmountBeforeDamage >= lastDamageTaken;
	}
	
	@Inject(at=@At("HEAD"), method="playHurtSound(Lnet/minecraft/entity/damage/DamageSource;)V",
			cancellable=true)
	public void playHurtSound(DamageSource src, CallbackInfo ci) {
		if (!MixinConfigPlugin.isEnabled("*.alt_absorption_sound")) return;
		Object self = this;
		if (fabrication$didJustAbsorp()) {
			PacketByteBuf data = new PacketByteBuf(Unpooled.buffer(4));
			data.writeInt(getId());
			CustomPayloadS2CPacket fabPkt = new CustomPayloadS2CPacket(new Identifier("fabrication", "play_absorp_sound"), data);
			SoundEvent defHurtSound = getHurtSound(src);
			PlaySoundFromEntityS2CPacket vanPkt = defHurtSound == null ? null : new PlaySoundFromEntityS2CPacket(defHurtSound, getSoundCategory(), this, getSoundVolume(), getSoundPitch());
			for (ServerPlayerEntity spe : FabricationMod.getTrackers(this)) {
				if (spe instanceof SetFabricationConfigAware && ((SetFabricationConfigAware) spe).fabrication$isConfigAware()) {
					spe.networkHandler.sendPacket(fabPkt);
				} else if (vanPkt != null) {
					spe.networkHandler.sendPacket(vanPkt);
				}
			}
			if (self instanceof ServerPlayerEntity) {
				ServerPlayerEntity selfp = (ServerPlayerEntity)self;
				if (selfp instanceof SetFabricationConfigAware && ((SetFabricationConfigAware) selfp).fabrication$isConfigAware()) {
					selfp.networkHandler.sendPacket(fabPkt);
				} else if (vanPkt != null) {
					selfp.networkHandler.sendPacket(vanPkt);
				}
			}
			ci.cancel();
		}
	}
	
}
