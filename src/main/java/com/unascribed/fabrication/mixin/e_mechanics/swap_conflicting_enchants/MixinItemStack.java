package com.unascribed.fabrication.mixin.e_mechanics.swap_conflicting_enchants;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryArrayList;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryHashMap;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryIdentifier;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryNbt;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryPair;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ItemStack.class)
@EligibleIf(configAvailable="*.swap_conflicting_enchants")
public abstract class MixinItemStack {

	@Shadow
	public abstract boolean hasEnchantments();

	@Shadow
	private NbtCompound nbt;

	@FabInject(method="use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", at=@At("HEAD"), cancellable=true)
	public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		if (!FabConf.isEnabled("*.swap_conflicting_enchants")) return;
		List<Pair<String, Integer>> currentConflicts = ForgeryArrayList.get();
		if (nbt == null) return;

		NbtCompound lTag = nbt.getCompound("fabrication#conflictingEnchants");
		if (lTag == null || lTag.isEmpty()) return;
		for (String key : lTag.getKeys()) {
			currentConflicts.add(ForgeryPair.get(key, lTag.getInt(key)));
		}
		if (!currentConflicts.isEmpty() && user.isSneaky()) {
			NbtCompound tag = ForgeryNbt.getCompound();
			Pair<String, Integer> toAdd;
			{
				int rmi = world.random.nextInt(currentConflicts.size());
				toAdd = currentConflicts.get(rmi);
				currentConflicts.remove(rmi);
			}
			Enchantment toAddEnchant = Registries.ENCHANTMENT.get(ForgeryIdentifier.get(toAdd.getLeft()));
			Map<Enchantment, Integer> currentEnchantments = ForgeryHashMap.get();
			currentEnchantments.put(toAddEnchant, toAdd.getRight());
			if (this.hasEnchantments()) {
				for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get((ItemStack)(Object)this).entrySet()) {
					if (entry.getKey().canCombine(toAddEnchant)) {
						currentEnchantments.put(entry.getKey(), entry.getValue());
					} else {
						tag.putInt(String.valueOf(Registries.ENCHANTMENT.getId(entry.getKey())), entry.getValue());
					}
				}
			}
			for (Pair<String, Integer> entry : currentConflicts) {
				Enchantment enchant = Registries.ENCHANTMENT.get(ForgeryIdentifier.get(entry.getLeft()));
				if (currentEnchantments.keySet().stream().anyMatch(e->!e.canCombine(enchant))) {
					tag.putInt(entry.getLeft(), entry.getRight());
					continue;
				}
				currentEnchantments.put(enchant, entry.getRight());
			}
			EnchantmentHelper.set(currentEnchantments, (ItemStack)(Object)this);
			if (!tag.isEmpty()) {
				nbt.put("fabrication#conflictingEnchants", tag);
			}
			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1, 1);
			cir.setReturnValue(TypedActionResult.consume((ItemStack)(Object)this));
		}
	}

}
