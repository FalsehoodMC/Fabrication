package com.unascribed.fabrication;

import java.util.UUID;
import java.util.function.Predicate;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unascribed.fabrication.support.MixinConfigPlugin;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListTag;

public class CanHitUtil {

	public static boolean canHit(ListTag list, Entity entity) {
		if (list == null) return true;
		try {
			for (int i = 0; i < list.size(); i++) {
				String s = list.getString(i);
				if (s.contains("-")) {
					try {
						UUID id = UUID.fromString(s);
						if (entity.getUuid().equals(id)) {
							return true;
						}
						continue;
					} catch (IllegalArgumentException ex) {}
				}
				if (s.startsWith("@")) {
					EntitySelector ep = new EntitySelectorReader(new StringReader(s), true).read();
					Predicate<Entity> predicate = ep.basePredicate;
					if (predicate.test(entity)) {
						return true;
					}
				} else {
					boolean needed = true;
					if (s.startsWith("!")) {
						s = s.substring(1);
						needed = false;
					}
					final String id = s.contains(":") ? s : "minecraft:"+s;
					if (EntityType.getId(entity.getType()).toString().equals(id) == needed) {
						return true;
					}
				}
			}
		} catch (CommandSyntaxException e) {}
		return false;
	}
	
	public static boolean canHit(ItemStack stack, Entity entity) {
		if (stack.hasTag() && stack.getTag().contains("CanHit", NbtType.LIST)) {
			ListTag canhit = stack.getTag().getList("CanHit", NbtType.STRING);
			return canHit(canhit, entity);
		}
		return true;
	}

	public static boolean isExempt(Entity shooter) {
		if (shooter instanceof PlayerEntity) {
			PlayerEntity p = (PlayerEntity)shooter;
			return p.abilities.creativeMode || (!MixinConfigPlugin.isEnabled("*.adventure_tags_in_survival") && p.canModifyBlocks());
		}
		return false;
	}

}
