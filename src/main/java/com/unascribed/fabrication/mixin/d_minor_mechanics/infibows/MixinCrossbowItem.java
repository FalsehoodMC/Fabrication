package com.unascribed.fabrication.mixin.d_minor_mechanics.infibows;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value=CrossbowItem.class, priority=1001)
@EligibleIf(anyConfigAvailable="*.infibows")
public class MixinCrossbowItem {

	@FabInject(at=@At(value="INVOKE", target="Ljava/util/List;isEmpty()Z"), method="loadProjectiles(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)Z", locals=LocalCapture.CAPTURE_FAILHARD)
	private static void fabrication$modifyCreativeModeLoadProjectile(LivingEntity shooter, ItemStack crossbow, CallbackInfoReturnable<Boolean> cir, List<ItemStack> list) {
		if (FabConf.isAnyEnabled("*.infibows") && EnchantmentHelperHelper.getLevel(shooter.getRegistryManager(), Enchantments.INFINITY, crossbow) > 0 && list.isEmpty()) {
			list.add(Items.ARROW.getDefaultStack());
		}
	}

}
