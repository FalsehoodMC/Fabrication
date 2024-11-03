package com.unascribed.fabrication.mixin.f_balance.tools_in_bundles;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.BundleHelper;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BundleContentsComponent.Builder.class)
@EligibleIf(configAvailable="*.tools_in_bundles")
public class MixinBundleContentsComponentBuilder {
	@Shadow
	@Final
	private List<ItemStack> stacks;

	@FabInject(at=@At("HEAD"), method="add(Lnet/minecraft/item/ItemStack;)I", cancellable=true)
	public void add(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (FabConf.isEnabled("*.tools_in_bundles") && !stack.isEmpty()) {
			if (!BundleHelper.isCompatible(this.stacks, stack)) {
				cir.setReturnValue(0);
			}
		}
	}

	@FabInject(at=@At("HEAD"), method="addInternal(Lnet/minecraft/item/ItemStack;)I", cancellable=true)
	public void addInternal(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (FabConf.isEnabled("*.tools_in_bundles") && stack.getMaxCount() == 1) {
			cir.setReturnValue(-1);
		}
	}
}
