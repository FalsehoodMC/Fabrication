package com.unascribed.fabrication.mixin._general.config;

import com.unascribed.fabrication.FabConf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

	@Shadow @Final
	protected LevelStorage.Session session;

	@FabInject(method="loadWorld()V", at=@At("HEAD"))
	public void getPath(CallbackInfo ci) {
		FabConf.setWorldPath(session.getDirectory(WorldSavePath.ROOT), true);
	}

}
