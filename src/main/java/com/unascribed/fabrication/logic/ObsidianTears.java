package com.unascribed.fabrication.logic;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potions;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class ObsidianTears {

	public static final ItemDispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior() {

		@Override
		protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
			BlockPos pos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
			List<ServerPlayerEntity> players = pointer.world().getEntitiesByClass(ServerPlayerEntity.class, new Box(pos), EntityPredicates.EXCEPT_SPECTATOR);
			if (players.isEmpty()) {
				return stack;
			} else {
				ObsidianTears.setSpawnPoint(players.get(ThreadLocalRandom.current().nextInt(players.size())), stack);
				return new ItemStack(Items.GLASS_BOTTLE);
			}
		}

	};

	public static void setSpawnPoint(ServerPlayerEntity p, ItemStack stack) {
		if (!stack.contains(DataComponentTypes.CUSTOM_DATA)) return;
		World world = p.getWorld();
		RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().getString("fabrication:ObsidianTearsOriginDim")));
		BlockPos pos = BlockPos.fromLong(stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().getLong("fabrication:ObsidianTearsOrigin"));
		if (world instanceof ServerWorld) {
			((ServerWorld)world).spawnParticles(ParticleTypes.FALLING_OBSIDIAN_TEAR, p.getPos().x, p.getPos().y+p.getBoundingBox().getLengthY()/2, p.getPos().z, 16,
					p.getBoundingBox().getLengthX()/2, p.getBoundingBox().getLengthY()/2, p.getBoundingBox().getLengthZ()/2,
					0.5);
		}
		p.setSpawnPoint(key, pos, p.getYaw(), false, true);
	}

	public static ItemStack createStack(World world, BlockPos blockPos) {
		ItemStack stack = Items.POTION.getDefaultStack();
		stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§fObsidian Tears"));
		NbtCompound tag = new NbtCompound();
		tag.putBoolean("fabrication:ObsidianTears", true);
		tag.putLong("fabrication:ObsidianTearsOrigin", blockPos.asLong());
		tag.putString("fabrication:ObsidianTearsOriginDim", world.getRegistryKey().getValue().toString());
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
		stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.of(Potions.THICK), Optional.of(0x540CB7), List.of()));
		stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
		return stack;
	}

}
