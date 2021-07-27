package com.unascribed.fabrication.mixin.a_fixes.adventure_tags_in_survival;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

@Mixin(ItemStack.class)
@EligibleIf(configEnabled="*.adventure_tags_in_survival")
public class MixinItemStack {

	@Inject(at=@At("HEAD"), method="useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;",
			cancellable=true)
	public void useOnBlock(ItemUsageContext iuc, CallbackInfoReturnable<ActionResult> ci) {
		if (!MixinConfigPlugin.isEnabled("*.adventure_tags_in_survival")) return;
		PlayerEntity player = iuc.getPlayer();
		if (player != null && (player.getAbilities().creativeMode || !player.getAbilities().allowModifyWorld)) return;
		ItemStack self = (ItemStack)(Object)this;
		if (!self.isEmpty()) {
			if (self.hasNbt() && self.getNbt().contains("CanPlaceOn")) {
				boolean able = self.canPlaceOn(iuc.getWorld().getTagManager(), new CachedBlockPosition(iuc.getWorld(), iuc.getBlockPos(), false));
				if (!able) {
					ci.setReturnValue(ActionResult.PASS);
				}
			}
		}
	}
	
}
