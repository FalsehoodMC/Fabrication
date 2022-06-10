package com.unascribed.fabrication.mixin.b_utility.disable_bees;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.world.gen.treedecorator.BeehiveTreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeehiveTreeDecorator.class)
@EligibleIf(configAvailable="*.disable_bees")
public class MixinBeehiveTreeDecorator {

	@Inject(at=@At("HEAD"), method= "generate(Lnet/minecraft/world/gen/treedecorator/TreeDecorator$Generator;)V",
			cancellable=true)
	public void generate(TreeDecorator.Generator generator, CallbackInfo ci) {
		if (FabConf.isEnabled("*.disable_bees")) {
			ci.cancel();
		}
	}

}
