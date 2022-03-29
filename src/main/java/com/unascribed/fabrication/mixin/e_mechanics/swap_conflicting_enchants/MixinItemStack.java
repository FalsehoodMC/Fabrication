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
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		List<Pair<String, Integer>> currentConflicts = new ArrayList<>();
		NbtCompound lTag = nbt.getCompound("fabrication#conflictingEnchants");
		if (lTag != null && !lTag.isEmpty()) {
			for (String key : lTag.getKeys()) {
				currentConflicts.add(new Pair<>(key, lTag.getInt(key)));
			}
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
			Map<Enchantment, Integer> currentEnchantments = null;
			if (this.hasEnchantments()) {
				currentEnchantments = EnchantmentHelper.get((ItemStack)(Object)this)
						.entrySet().stream().filter(entry -> {
							if (!entry.getKey().canCombine(toAddEnchant)) {
								tag.putInt(String.valueOf(Registry.ENCHANTMENT.getId(entry.getKey())), entry.getValue());
								return false;
							}
							return true;
						}).collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
				EnchantmentHelper.set(currentEnchantments, (ItemStack)(Object)this);
			}
			for (Pair<String, Integer> entry : currentConflicts) {
				Enchantment enchant = Registry.ENCHANTMENT.get(new Identifier(entry.getLeft()));
				if (currentEnchantments != null && currentEnchantments.keySet().stream().anyMatch(e->!e.canCombine(enchant))) {
					tag.putInt(entry.getLeft(), entry.getRight());
				} else {
					addEnchantment(enchant, entry.getRight());
				}
			}
			if (!tag.isEmpty()) {
				nbt.put("fabrication#conflictingEnchants", tag);
			}
			addEnchantment(toAddEnchant, toAdd.getRight());
			world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1, 1);
			cir.setReturnValue(TypedActionResult.consume((ItemStack)(Object)this));
		}
	}

}
