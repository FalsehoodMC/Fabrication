package com.unascribed.fabrication.mixin.b_utility.taggable_players;

import java.util.List;
import java.util.Random;

import com.unascribed.fabrication.FabConf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.logic.PlayerTag;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WanderingTraderManager;

@Mixin(WanderingTraderManager.class)
@EligibleIf(configAvailable="*.taggable_players")
public abstract class MixinWanderingTraderManager {

	@Shadow @Final private Random random;

	@ModifyVariable(at=@At(value="STORE", ordinal=0), method= "trySpawn(Lnet/minecraft/server/world/ServerWorld;)Z")
	protected PlayerEntity trySpawn(PlayerEntity old,ServerWorld world) {
		if (!FabConf.isEnabled("*.taggable_players")) return old;
		List<ServerPlayerEntity> list = world.getPlayers(pe -> pe.isAlive() && !(pe instanceof TaggablePlayer && ((TaggablePlayer)pe).fabrication$hasTag(PlayerTag.NO_WANDERING_TRADER)));
		return list.isEmpty() ? null : list.get(this.random.nextInt(list.size()));
	}

}
