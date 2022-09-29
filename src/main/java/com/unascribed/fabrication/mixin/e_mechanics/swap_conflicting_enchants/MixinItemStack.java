package com.unascribed.fabrication.mixin.e_mechanics.swap_conflicting_enchants;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		List<Pair<String, Integer>> currentConflicts = new ArrayList<>();
		if (nbt == null) return;
		NbtCompound lTag = nbt.getCompound("fabrication#conflictingEnchants");
		if (lTag == null || lTag.isEmpty()) return;
		for (String key : lTag.getKeys()) {
			currentConflicts.add(new Pair<>(key, lTag.getInt(key)));
		}
		if (!currentConflicts.isEmpty() && user.isSneaky()) {
			NbtCompound tag = new NbtCompound();
			Pair<String, Integer> toAdd;
			{
				int rmi = world.random.nextInt(currentConflicts.size());
				toAdd = currentConflicts.get(rmi);
				currentConflicts.remove(rmi);
			}
			Enchantment toAddEnchant = Registry.ENCHANTMENT.get(new Identifier(toAdd.getLeft()));
			Map<Enchantment, Integer> currentEnchantments = new HashMap<>();
			currentEnchantments.put(toAddEnchant, toAdd.getRight());
			if (this.hasEnchantments()) {
				for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.get((ItemStack)(Object)this).entrySet()) {
					if (entry.getKey().canCombine(toAddEnchant)) {
						currentEnchantments.put(entry.getKey(), entry.getValue());
					} else {
						tag.putInt(String.valueOf(Registry.ENCHANTMENT.getId(entry.getKey())), entry.getValue());
					}
				}
			}
			for (Pair<String, Integer> entry : currentConflicts) {
				Enchantment enchant = Registry.ENCHANTMENT.get(new Identifier(entry.getLeft()));
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
