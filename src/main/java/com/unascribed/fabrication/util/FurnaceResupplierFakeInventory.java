package com.unascribed.fabrication.util;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabRefl;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FurnaceResupplierFakeInventory extends Entity implements SidedInventory {
	public ItemStack stack = ItemStack.EMPTY;
	public FurnaceResupplierFakeInventory(World world) {
		super(EntityType.EGG, world);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {
	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {
	}

	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
		return null;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.stack.isEmpty();
	}

	@Override
	public ItemStack getStack(int slot) {
		if (slot != 0) return ItemStack.EMPTY;
		return this.stack;
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		if (slot != 0) return ItemStack.EMPTY;
		return this.stack.split(amount);
	}

	@Override
	public ItemStack removeStack(int slot) {
		if (slot != 0) return ItemStack.EMPTY;
		ItemStack ret = this.stack;
		this.stack = ItemStack.EMPTY;
		return ret;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		if (slot != 0) return;
		this.stack = stack;
	}

	@Override
	public void markDirty() {
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return false;
	}

	@Override
	public void clear() {
		this.stack = ItemStack.EMPTY;
	}
	public int getMaxCountPerStack() {
		return 1;
	}

	public static int[] SLOTS = new int[]{0};
	@Override
	public int[] getAvailableSlots(Direction side) {
		return SLOTS;
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, Direction dir) {
		if (!FabConf.isEnabled("*.furnace_minecart_resupplying")) return false;
		if (slot != 0 || !this.stack.isEmpty()) return false;
		if (FabConf.isEnabled("*.furnace_minecart_any_fuel")) return FurnaceBlockEntity.canUseAsFuel(stack);
		return  FabRefl.getAcceltableFuel().test(stack);
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return false;
	}
}
