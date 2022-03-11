package com.unascribed.fabrication.mixin.e_mechanics.swap_conflicting_enchants;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(ItemStack.class)
@EligibleIf(configAvailable="*.swap_conflicting_enchants")
public abstract class MixinItemStack {

	@Shadow
	public abstract boolean hasEnchantments();

	@Shadow
	public abstract void addEnchantment(Enchantment enchantment, int level);

	@Shadow
	private NbtCompound nbt;

	@Inject(method="use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;", at=@At("HEAD"), cancellable=true)
	public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		if (!FabConf.isEnabled("*.swap_conflicting_enchants")) return;
		LinkedHashMap<Enchantment, Integer> currentConflicts = new LinkedHashMap<>();
		NbtCompound tag = nbt.getCompound("fabrication#conflictingEnchants");
		if (tag != null && !tag.isEmpty()) {
			for (String key : tag.getKeys()) {
				currentConflicts.put(Registry.ENCHANTMENT.get(new Identifier(key)), tag.getInt(key));
			}
		}
		if (!currentConflicts.isEmpty() && user.isSneaky()) {
			Map.Entry<Enchantment, Integer> toAdd;
			{
				Iterator<Map.Entry<Enchantment, Integer>> iter = currentConflicts.entrySet().iterator();
				toAdd = iter.next();
				iter.remove();
			}
			if (this.hasEnchantments()) {
				Map<Enchantment, Integer> conflictingEnchants = new HashMap<>();
				Map<Enchantment, Integer> enchants = EnchantmentHelper.get((ItemStack)(Object)this);
				Enchantment[] enchantList = enchants.keySet().toArray(new Enchantment[0]);
				for (Enchantment enchantment : enchantList) {
					if (!enchantment.canCombine(toAdd.getKey())) {
						conflictingEnchants.put(enchantment, enchants.get(enchantment));
						enchants.remove(enchantment);
						break;
					}
				}
				currentConflicts.putAll(conflictingEnchants);
				EnchantmentHelper.set(enchants, (ItemStack)(Object)this);
			}
			NbtCompound conflictingEnchants = new NbtCompound();
			for (Map.Entry<Enchantment, Integer> entry : currentConflicts.entrySet()) {
				conflictingEnchants.putInt(String.valueOf(Registry.ENCHANTMENT.getId(entry.getKey())), entry.getValue());
			}
			if (!conflictingEnchants.isEmpty()) {
				nbt.put("fabrication#conflictingEnchants", conflictingEnchants);
			}
			addEnchantment(toAdd.getKey(), toAdd.getValue());
			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1, 1);
			cir.setReturnValue(TypedActionResult.consume((ItemStack)(Object)this));
		}
	}

}
