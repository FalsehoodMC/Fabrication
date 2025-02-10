package com.unascribed.fabrication.logic;

import java.util.UUID;
import java.util.function.Predicate;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unascribed.fabrication.FabConf;

import com.unascribed.fabrication.FabRefl;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class CanHitUtil {

	public static boolean canHit(NbtList list, Entity entity) {
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
					} catch (IllegalArgumentException ex) {
					}
				}
				if (s.startsWith("@")) {
					EntitySelector ep = new EntitySelectorReader(new StringReader(s), true).read();
					for (Predicate<Entity> predicate : FabRefl.getBasePredicate(ep)) {
						if (!predicate.test(entity)) return false;
					}
					return true;
				} else {
					boolean needed = true;
					if (s.startsWith("!")) {
						s = s.substring(1);
						needed = false;
					}
					final String id = s.contains(":") ? s : "minecraft:" + s;
					if (EntityType.getId(entity.getType()).toString().equals(id) == needed) {
						return true;
					}
				}
			}
		} catch (CommandSyntaxException e) {}
		return false;
	}

	public static boolean canHit(ItemStack stack, Entity entity) {
		if (stack.contains(DataComponentTypes.CUSTOM_DATA) && stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().contains("CanHit", NbtElement.LIST_TYPE)) {
			NbtList canhit = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().getList("CanHit", NbtElement.STRING_TYPE);
			return canHit(canhit, entity);
		}
		return true;
	}

	public static boolean isExempt(Entity shooter) {
		if (shooter instanceof PlayerEntity) {
			PlayerEntity p = (PlayerEntity)shooter;
			return p.getAbilities().creativeMode || (!FabConf.isEnabled("*.adventure_tags_in_survival") && p.canModifyBlocks());
		}
		return false;
	}

}
