package com.unascribed.fabrication.mixin.c_tweaks.no_wandering_trader;

import java.util.List;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WanderingTraderManager;

@Mixin(WanderingTraderManager.class)
@EligibleIf(configAvailable="*.no_wandering_trader")
public abstract class MixinWanderingTraderManager {

	@Shadow @Final private Random random;

	@FabModifyVariable(at=@At(value="STORE", ordinal=0), method="trySpawn(Lnet/minecraft/server/world/ServerWorld;)Z")
	protected PlayerEntity trySpawn(PlayerEntity old,ServerWorld world) {
		if (!FabConf.isEnabled("*.no_wandering_trader")) return old;
		List<ServerPlayerEntity> list = world.getPlayers(pe -> pe.isAlive() && !ConfigPredicates.shouldRun("*.no_wandering_trader", (PlayerEntity)pe));
		return list.isEmpty() ? null : list.get(this.random.nextInt(list.size()));
	}

}
