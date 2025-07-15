package com.unascribed.fabrication.logic;

import com.unascribed.fabrication.FabRefl;

import com.unascribed.fabrication.support.ConfigPredicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public class InstantPickup {

	private static final Predicate<PlayerEntity> fabrication$instantPickupPredicate = ConfigPredicates.getFinalPredicate("*.instant_pickup");
	public static void slurp(World world, Box box, PlayerEntity breaker) {
		if (!fabrication$instantPickupPredicate.test(breaker)) return;
		for (ItemEntity ie : world.getEntitiesByType(EntityType.ITEM, box, (e) -> ((Entity)e).age == 0)) {
			if (!ie.isAlive()) continue;
			int oldPickupDelay = FabRefl.getPickupDelay(ie);
			ie.setPickupDelay(0);
			ie.getCommandTags().add("interactic.ignore_auto_pickup_rule");
			ie.onPlayerCollision(breaker);
			if (ie.isAlive()) {
				ie.setPickupDelay(oldPickupDelay);
				ie.getCommandTags().remove("interactic.ignore_auto_pickup_rule");
			}
		}
	}

}
