package com.unascribed.fabrication.mixin.f_balance.chest_pigs;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.injection.FabInject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=PigEntity.class, priority=1001)
@EligibleIf(configAvailable="*.chest_pigs")
@FailOn(modLoaded="haulinghog")
public abstract class MixinPigEntity extends Entity {
	@Unique
	private SimpleInventory fabrication$chestPig = null;

	public MixinPigEntity(EntityType<?> type, World world) {
		super(type, world);
	}
	@FabInject(method="dropInventory()V", at=@At("HEAD"))
	protected void dropInventory(CallbackInfo info) {
		if (fabrication$chestPig != null) {
			switch (fabrication$chestPig.size()) {
				case 53:
					this.dropItem(Items.CHEST);
				case 27:
					this.dropItem(Items.CHEST);
					break;
				case 0:
					this.dropItem(Items.ENDER_CHEST);
					break;
			}
			for (ItemStack item : fabrication$chestPig.clearToList()) {
				this.dropStack(item);
			}
		}
	}
	@FabInject(method="interactMob(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at=@At("HEAD"), cancellable=true)
	private void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
		if (!FabConf.isEnabled("*.chest_pigs")) return;
		if (player.isSneaking()) {
			World world = this.getWorld();
			if (fabrication$chestPig == null) {
				ItemStack stack = player.getInventory().getMainHandStack();
				boolean isChest = stack.getItem().equals(Items.CHEST);
				if (isChest || stack.getItem().equals(Items.ENDER_CHEST)) {
					info.setReturnValue(ActionResult.SUCCESS);
					stack.decrement(1);
					fabrication$chestPig = new SimpleInventory(isChest ? 27 : 0);
					world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.NEUTRAL, 0.2F, 0.4F);
					world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_PIG_AMBIENT, SoundCategory.NEUTRAL, 0.7F, 0.1F);
				}
			} else {
				info.setReturnValue(ActionResult.SUCCESS);
				int invSize = fabrication$chestPig.size();
				switch (invSize) {
					case 0:
						player.openHandledScreen(new SimpleNamedScreenHandlerFactory(new ScreenHandlerFactory() {
							@Override
							public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
								return GenericContainerScreenHandler.createGeneric9x3(syncId, inv, player.getEnderChestInventory());
							}
						}, Text.translatable("container.enderchest")));
						break;
					case 27:
						ItemStack stack = player.getInventory().getMainHandStack();
						if (stack.getItem().equals(Items.CHEST)) {
							stack.decrement(1);
							SimpleInventory temp = new SimpleInventory(54);
							for (ItemStack item : fabrication$chestPig.clearToList()){
								temp.addStack(item);
							}
							fabrication$chestPig = temp;
							world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.NEUTRAL, 0.2F, 0.4F);
							world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_PIG_AMBIENT, SoundCategory.NEUTRAL, 0.7F, 0.1F);
							return;
						}
						player.openHandledScreen(new SimpleNamedScreenHandlerFactory(new ScreenHandlerFactory() {
							@Override
							public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
								return GenericContainerScreenHandler.createGeneric9x3(syncId, inv, fabrication$chestPig);
							}
						}, Text.translatable("container.chest")));
						break;
					case 54:
						player.openHandledScreen(new SimpleNamedScreenHandlerFactory(new ScreenHandlerFactory() {
							@Override
							public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
								return GenericContainerScreenHandler.createGeneric9x6(syncId, inv, fabrication$chestPig);
							}
						}, Text.translatable("container.chest")));
						break;
				}
			}
		}
	}
	@FabInject(method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", at=@At("HEAD"))
	public void writeCustomDataToTag(NbtCompound tags, CallbackInfo info) {
		if (fabrication$chestPig != null) {
			NbtCompound tag = new NbtCompound();
			tags.putByte("fabrication$chestPigs$size", (byte)fabrication$chestPig.size());
			for (byte i=0; i<fabrication$chestPig.size();++i) {
				NbtElement tagi = fabrication$chestPig.getStack(i).encodeAllowEmpty(this.getRegistryManager());
				tag.put(""+i, tagi);
			}
			tags.put("fabrication$chestPigs$inv",tag);
		}
	}
	@FabInject(method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", at=@At("HEAD"))
	public void readCustomDataFromTag(NbtCompound tags, CallbackInfo info) {
		NbtCompound tag = tags.getCompound("fabrication$chestPigs$inv");
		if (tag == null || !tags.contains("fabrication$chestPigs$size")) return;
		byte size = tags.getByte("fabrication$chestPigs$size");
		switch (size) {
			case 0:
				fabrication$chestPig = new SimpleInventory(0);
				break;
			case 27:
			case 54:
				fabrication$chestPig = new SimpleInventory(size);
				for (byte i=0; i<size; ++i){
					fabrication$chestPig.setStack(i, ItemStack.fromNbtOrEmpty(this.getRegistryManager(), tag.getCompound(""+i)));
				}
				break;
			default:
				fabrication$chestPig = null;
		}

	}
}
