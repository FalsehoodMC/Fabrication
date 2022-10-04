package com.unascribed.fabrication.mixin.a_fixes.fix_nether_portal_nausea;

import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.PortalRenderFix;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
@EligibleIf(configAvailable="*.fix_nether_portal_nausea", envMatches=Env.CLIENT)
public abstract class MixinClientPlayerEntity extends PlayerEntity implements PortalRenderFix {

	private float fabrication$lastClientPortalTicks = 0;
	private float fabrication$nextClientPortalTicks = 0;

	public MixinClientPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile, PlayerPublicKey publicKey) {
		super(world, pos, yaw, gameProfile, publicKey);
	}

	@FabInject(method="tickMovement()V", at=@At("HEAD"))
	private void fixPortalNausea(CallbackInfo ci){
		if (!FabConf.isEnabled("*.fix_nether_portal_nausea")) return;
		fabrication$lastClientPortalTicks = fabrication$nextClientPortalTicks;
		if (inNetherPortal) {
			if (fabrication$nextClientPortalTicks < 1.0F){
				fabrication$nextClientPortalTicks += 0.0125F;
			}else if (fabrication$nextClientPortalTicks >= 1.0F){
				fabrication$nextClientPortalTicks = 1.0F;
			}
		}else {
			if (this.fabrication$nextClientPortalTicks > 0.0F) {
				this.fabrication$nextClientPortalTicks -= 0.05F;
			}
			if (this.fabrication$nextClientPortalTicks < 0.0F) {
				this.fabrication$nextClientPortalTicks = 0.0F;
			}
		}
	}

	public boolean fabrication$shouldRenderPortal(){
		return fabrication$nextClientPortalTicks > 0 && fabrication$lastClientPortalTicks > 0 && hasStatusEffect(StatusEffects.NAUSEA);
	}

	public float fabrication$getPortalRenderProgress(float tickDelta){
		return MathHelper.lerp(tickDelta, fabrication$lastClientPortalTicks, fabrication$nextClientPortalTicks);
	}

}
