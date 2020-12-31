package com.unascribed.fabrication.mixin.b_utility.item_despawn;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.ParsedTime;
import com.unascribed.fabrication.Resolvable;
import com.unascribed.fabrication.interfaces.SetFromPlayerDeath;
import com.unascribed.fabrication.loaders.LoaderItemDespawn;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import com.google.common.primitives.Ints;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(ItemEntity.class)
@EligibleIf(configEnabled="*.item_despawn")
public abstract class MixinItemEntity extends Entity implements SetFromPlayerDeath {

	public MixinItemEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private long fabrication$trueAge;
	private int fabrication$extraTime;
	private boolean fabrication$invincible;
	private boolean fabrication$fromPlayerDeath;
	
	@Shadow
	private int age;
	@Shadow
	private UUID thrower;
	
	@Shadow
	public abstract ItemStack getStack();
	
	@Inject(at=@At("HEAD"), method="tick()V")
	public void tickHead(CallbackInfo ci) {
		if (fabrication$extraTime > 0) {
			fabrication$extraTime--;
			age--;
		}
		fabrication$trueAge++;
		if (getPos().y < -32) {
			if (fabrication$invincible) {
				teleport(getPos().x, 1, getPos().z);
				setVelocity(0,0,0);
				if (!world.isClient) {
					((ServerWorld)world).getChunkManager().sendToNearbyPlayers(this, new EntityPositionS2CPacket(this));
					((ServerWorld)world).getChunkManager().sendToNearbyPlayers(this, new EntityVelocityUpdateS2CPacket(this));
				}
			}
		}
	}
	
	@Inject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable=true)
	public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
		if (fabrication$invincible || (RuntimeChecks.check("*.item_despawn") && world.isClient)) {
			ci.setReturnValue(false);
		}
	}
	
	@Inject(at=@At("TAIL"), method="setStack(Lnet/minecraft/item/ItemStack;)V")
	public void setStack(ItemStack stack, CallbackInfo ci) {
		calculateDespawn();
	}
	
	@Inject(at=@At("TAIL"), method="setThrower(Ljava/util/UUID;)V")
	public void setThrower(UUID id, CallbackInfo ci) {
		calculateDespawn();
	}
	
	@Override
	public void fabrication$setFromPlayerDeath(boolean b) {
		fabrication$fromPlayerDeath = b;
		calculateDespawn();
	}

	@Unique
	private void calculateDespawn() {
		if (world.isClient) return;
		ItemStack stack = getStack();
		ParsedTime time = ParsedTime.UNSET;
		ParsedTime itemTime = LoaderItemDespawn.itemDespawns.get(Resolvable.mapKey(stack.getItem(), Registry.ITEM));
		if (itemTime != null) {
			time = itemTime;
		}
		if (!time.priority) {
			for (Enchantment e : EnchantmentHelper.get(stack).keySet()) {
				if (e.isCursed()) {
					if (LoaderItemDespawn.curseDespawn.overshadows(time)) {
						time = LoaderItemDespawn.curseDespawn;
					}
				} else {
					if (LoaderItemDespawn.normalEnchDespawn.overshadows(time)) {
						time = LoaderItemDespawn.normalEnchDespawn;
					}
					if (e.isTreasure()) {
						if (LoaderItemDespawn.treasureDespawn.overshadows(time)) {
							time = LoaderItemDespawn.treasureDespawn;
						}
					}
				}
				ParsedTime enchTime = LoaderItemDespawn.enchDespawns.get(Resolvable.mapKey(e, Registry.ENCHANTMENT));
				if (enchTime != null && enchTime.overshadows(time)) {
					time = enchTime;
				}
			}
			for (Map.Entry<Identifier, ParsedTime> en : LoaderItemDespawn.tagDespawns.entrySet()) {
				Tag<Item> itemTag = ItemTags.getTagGroup().getTag(en.getKey());
				if (itemTag != null && itemTag.contains(stack.getItem())) {
					if (en.getValue().overshadows(time)) {
						time = en.getValue();
					}
				}
				if (stack.getItem() instanceof BlockItem) {
					BlockItem bi = (BlockItem)stack.getItem();
					Tag<Block> blockTag = BlockTags.getTagGroup().getTag(en.getKey());
					if (blockTag != null && blockTag.contains(bi.getBlock())) {
						if (en.getValue().overshadows(time)) {
							time = en.getValue();
						}
					}
				}
			}
			if (stack.hasTag()) {
				for (Map.Entry<String, ParsedTime> en : LoaderItemDespawn.nbtBools.entrySet()) {
					if (stack.getTag().getBoolean(en.getKey())) {
						if (en.getValue().overshadows(time)) {
							time = en.getValue();
						}
					}
				}
			}
		}
		if (fabrication$fromPlayerDeath && LoaderItemDespawn.playerDeathDespawn.overshadows(time)) {
			time = LoaderItemDespawn.playerDeathDespawn;
		}
		if (time == ParsedTime.UNSET) {
			time = thrower == null ? LoaderItemDespawn.dropsDespawn : LoaderItemDespawn.defaultDespawn;
		}
		fabrication$invincible = false;
		if (time == ParsedTime.FOREVER) {
			fabrication$extraTime = 0;
			age = -32768;
		} else if (time == ParsedTime.INVINCIBLE) {
			fabrication$extraTime = 0;
			age = -32768;
			fabrication$invincible = true;
		} else if (time == ParsedTime.INSTANTLY) {
			remove();
		} else if (time == ParsedTime.UNSET) {
			fabrication$extraTime = 0;
		} else {
			int extra = time.timeInTicks-6000;
			extra -= Ints.saturatedCast(fabrication$trueAge);
			if (extra < 0) {
				age = -extra;
				fabrication$extraTime = 0;
			} else {
				age = 0;
				fabrication$extraTime = extra;
			}
		}
	}
	
	@Inject(at=@At("TAIL"), method="writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V")
	public void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
		if (fabrication$extraTime > 0) tag.putInt("fabrication:ExtraTime", fabrication$extraTime);
		tag.putLong("fabrication:TrueAge", fabrication$trueAge);
		if (fabrication$fromPlayerDeath) tag.putBoolean("fabrication:FromPlayerDeath", true);
		if (fabrication$invincible) tag.putBoolean("fabrication:Invincible", true);
	}
	
	@Inject(at=@At("TAIL"), method="readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V")
	public void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
		fabrication$extraTime = tag.getInt("fabrication:ExtraTime");
		fabrication$trueAge = tag.getLong("fabrication:TrueAge");
		fabrication$fromPlayerDeath = tag.getBoolean("fabrication:FromPlayerDeath");
		fabrication$invincible = tag.getBoolean("fabrication:Invincible");
	}
	
}
