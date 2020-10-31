package com.unascribed.fabrication.mixin.a_fixes.use_player_list_name_in_tag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
@EligibleIf(configEnabled="*.use_player_list_name_in_tag", envMatches=Env.CLIENT)
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at=@At("RETURN"), method="getDisplayName()Lnet/minecraft/text/Text;", cancellable=true)
	public void getDisplayName(CallbackInfoReturnable<Text> ci) {
		if (!RuntimeChecks.check("*.use_player_list_name_in_tag")) return;
		Object self = this;
		if (self instanceof AbstractClientPlayerEntity) {
			PlayerListEntry ple = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(getUuid());
			if (ple != null && ple.getDisplayName() != null) {
				ci.setReturnValue(ple.getDisplayName());
			}
		}
	}
	
}
