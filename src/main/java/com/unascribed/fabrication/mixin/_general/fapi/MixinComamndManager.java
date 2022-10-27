package com.unascribed.fabrication.mixin._general.fapi;

import com.mojang.brigadier.CommandDispatcher;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FabricationEvents;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
@EligibleIf(specialConditions=SpecialEligibility.NOT_FORGE)
public class MixinComamndManager {

		@Shadow @Final
		private CommandDispatcher<ServerCommandSource> dispatcher;

		@FabInject(method="<init>(Lnet/minecraft/server/command/CommandManager$RegistrationEnvironment;Lnet/minecraft/command/CommandRegistryAccess;)V", at=@At("TAIL"))
		private void addCommands(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci) {
			FabricationEvents.commands(this.dispatcher, commandRegistryAccess);
		}

}
