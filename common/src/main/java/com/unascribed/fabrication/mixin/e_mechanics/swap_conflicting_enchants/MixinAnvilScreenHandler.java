package com.unascribed.fabrication.mixin.e_mechanics.swap_conflicting_enchants;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryEnchantArray;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryIdentifier;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryNbt;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value=AnvilScreenHandler.class, priority=999)
@EligibleIf(configAvailable="*.swap_conflicting_enchants")
public abstract class MixinAnvilScreenHandler extends ForgingScreenHandler {


	public MixinAnvilScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
	}

	@ModifyReturn(method="updateResult()V", target="Lnet/minecraft/enchantment/Enchantment;canCombine(Lnet/minecraft/enchantment/Enchantment;)Z")
	private static boolean fabrication$allowConflictingEnchants(boolean old, Enchantment e1, Enchantment e2) {
		return old || FabConf.isEnabled("*.swap_conflicting_enchants") && e1 != e2;
	}

	@ModifyReturn(method="updateResult()V", target="Lnet/minecraft/enchantment/EnchantmentHelper;get(Lnet/minecraft/item/ItemStack;)Ljava/util/Map;")
	private static Map<Enchantment, Integer> fabrication$loadConflictingEnchants(Map<Enchantment, Integer> old, ItemStack stack) {
		if (FabConf.isEnabled("*.swap_conflicting_enchants")) {
			NbtCompound tag = stack.getSubNbt("fabrication#conflictingEnchants");
			if (tag != null && !tag.isEmpty()) {
				for (String key : tag.getKeys()) {
					old.put(Registry.ENCHANTMENT.get(ForgeryIdentifier.get(key)), tag.getInt(key));
				}
			}
		}
		return old;
	}

	@FabInject(at=@At("TAIL"), method="updateResult()V")
	public void allowCombiningIncompatibleEnchants(CallbackInfo ci) {
		if (!FabConf.isEnabled("*.swap_conflicting_enchants")) return;
		ItemStack stack = output.getStack(0);
		if (stack.hasEnchantments()) {
			NbtCompound conflictingEnchants = ForgeryNbt.getCompound();
			Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
			Enchantment[] enchantList = enchants.keySet().toArray(ForgeryEnchantArray.get(0));
			for (int i=0; i<enchantList.length; i++) {
				for (int ii=i+1; ii<enchantList.length; ii++){
					if (!enchantList[i].canCombine(enchantList[ii])) {
						conflictingEnchants.putInt(String.valueOf(Registry.ENCHANTMENT.getId(enchantList[i])), enchants.get(enchantList[i]));
						enchants.remove(enchantList[i]);
						break;
					}
				}
			}
			if (!conflictingEnchants.isEmpty()) {
				EnchantmentHelper.set(enchants, stack);
				stack.setSubNbt("fabrication#conflictingEnchants", conflictingEnchants);
			}
		}
	}


}
