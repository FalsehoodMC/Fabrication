package com.unascribed.fabrication.mixin.e_mechanics.swap_conflicting_enchants;

import com.google.common.collect.Lists;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryIdentifier;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryNbt;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(value=AnvilScreenHandler.class, priority=999)
@EligibleIf(configAvailable="*.swap_conflicting_enchants")
public abstract class MixinAnvilScreenHandler extends ForgingScreenHandler {

	public MixinAnvilScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
	}

	@ModifyReturn(method="updateResult()V", target="Lnet/minecraft/enchantment/Enchantment;canBeCombined(Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/registry/entry/RegistryEntry;)Z")
	private static boolean fabrication$allowConflictingEnchants(boolean old, RegistryEntry<Enchantment> e1, RegistryEntry<Enchantment> e2) {
		return old || FabConf.isEnabled("*.swap_conflicting_enchants") && e1 != e2;
	}

	@ModifyReturn(method="updateResult()V", target="Lnet/minecraft/enchantment/EnchantmentHelper;getEnchantments(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/component/type/ItemEnchantmentsComponent;")
	private ItemEnchantmentsComponent fabrication$loadConflictingEnchants(ItemEnchantmentsComponent old, ItemStack stack) {
		if (FabConf.isEnabled("*.swap_conflicting_enchants") && stack.contains(DataComponentTypes.CUSTOM_DATA)) {
			NbtCompound tag = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().getCompound("fabrication#conflictingEnchants");
			if (tag != null && !tag.isEmpty()) {
				Registry<Enchantment> registry = this.player.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
				ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(old);
				for (String key : tag.getKeys()) {
					Optional<RegistryEntry.Reference<Enchantment>> enchantment = registry.getEntry(ForgeryIdentifier.get(key));
					if (enchantment.isPresent()) {
						builder.add(enchantment.get(), tag.getInt(key));
					}
				}
				return builder.build();
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
			ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(stack.getEnchantments());
			List<RegistryEntry<Enchantment>> enchantList = Lists.newArrayList(builder.getEnchantments());
			for (int i=0; i<enchantList.size(); i++) {
				for (int ii=i+1; ii<enchantList.size(); ii++) {
					if (!Enchantment.canBeCombined(enchantList.get(i), enchantList.get(ii))) {
						RegistryEntry<Enchantment> removed = enchantList.get(i);
						conflictingEnchants.putInt(removed.getIdAsString(), builder.getLevel(removed));
						builder.remove(entry -> entry.matches(removed));
					}
				}
			}
			if (!conflictingEnchants.isEmpty()) {
				EnchantmentHelper.set(stack, builder.build());
				NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
				nbt.put("fabrication#conflictingEnchants", conflictingEnchants);
				stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
			}
		}
	}


}
