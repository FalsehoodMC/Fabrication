package com.unascribed.fabrication.mixin.d_minor_mechanics.gradual_block_breaking;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerInteractionManager.class)
public interface AccessorServerPlayerInteractionManager {
	@Accessor("player")
	ServerPlayerEntity fabrication$getPlayer();
}
