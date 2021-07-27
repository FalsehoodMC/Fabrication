package com.unascribed.fabrication.mixin.b_utility.item_despawn;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.ParsedTime;
import com.unascribed.fabrication.Resolvable;
import com.unascribed.fabrication.interfaces.SetFromPlayerDeath;
import com.unascribed.fabrication.loaders.LoaderItemDespawn;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
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
import net.minecraft.nbt.NbtCompound;
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
	private int itemAge;
	@Shadow
	private UUID thrower;
	
	@Shadow
	public abstract ItemStack getStack();
	
	@Inject(at=@At("HEAD"), method="tick()V")
	public void tickHead(CallbackInfo ci) {
		if (fabrication$extraTime > 0) {
			fabrication$extraTime--;
			itemAge--;
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
		if (fabrication$invincible || (MixinConfigPlugin.isEnabled("*.item_despawn") && world.isClient)) {
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
	
	@ModifyConstant(constant=@Constant(intValue=-32768), method="canMerge()Z")
	public int modifyIllegalAge(int orig) {
		// age-1 will never be equal to age; short-circuits the "age != -32768" check and allows
		// items set to "invincible" to stack together
		return fabrication$invincible ? itemAge -1 : orig;
	}
	
	@Override
	public void fabrication$setFromPlayerDeath(boolean b) {
		fabrication$fromPlayerDeath = b;
		calculateDespawn();
	}

	@Unique
	private void calculateDespawn() {
		if (world.isClient) return;
		final boolean debug = false;
		ItemStack stack = getStack();
		ParsedTime time = ParsedTime.UNSET;
		ParsedTime itemTime = LoaderItemDespawn.itemDespawns.get(Resolvable.mapKey(stack.getItem(), Registry.ITEM));
		if (debug) System.out.println("itemTime: "+itemTime);
		if (itemTime != null) {
			time = itemTime;
		}
		if (!time.priority) {
			if (debug) System.out.println("Not priority, check enchantments");
			for (Enchantment e : EnchantmentHelper.get(stack).keySet()) {
				if (e.isCursed()) {
					if (LoaderItemDespawn.curseDespawn.overshadows(time)) {
						if (debug) System.out.println("Found a curse; curseDespawn overshadows: "+LoaderItemDespawn.curseDespawn);
						time = LoaderItemDespawn.curseDespawn;
					}
				} else {
					if (LoaderItemDespawn.normalEnchDespawn.overshadows(time)) {
						if (debug) System.out.println("Found an enchantment; normalEnchDespawn overshadows: "+LoaderItemDespawn.normalEnchDespawn);
						time = LoaderItemDespawn.normalEnchDespawn;
					}
					if (e.isTreasure()) {
						if (LoaderItemDespawn.treasureDespawn.overshadows(time)) {
							if (debug) System.out.println("Found a treasure enchantment; treasureDespawn overshadows: "+LoaderItemDespawn.treasureDespawn);
							time = LoaderItemDespawn.treasureDespawn;
						}
					}
				}
				ParsedTime enchTime = LoaderItemDespawn.enchDespawns.get(Resolvable.mapKey(e, Registry.ENCHANTMENT));
				if (enchTime != null && enchTime.overshadows(time)) {
					if (debug) System.out.println("Found a specific enchantment; it overshadows: "+enchTime);
					time = enchTime;
				}
			}
			for (Map.Entry<Identifier, ParsedTime> en : LoaderItemDespawn.tagDespawns.entrySet()) {
				Tag<Item> itemTag = ItemTags.getTagGroup().getTag(en.getKey());
				if (itemTag != null && itemTag.contains(stack.getItem())) {
					if (en.getValue().overshadows(time)) {
						if (debug) System.out.println("Found a tag; it overshadows: "+en.getValue());
						time = en.getValue();
					}
				}
				if (stack.getItem() instanceof BlockItem) {
					BlockItem bi = (BlockItem)stack.getItem();
					Tag<Block> blockTag = BlockTags.getTagGroup().getTag(en.getKey());
					if (blockTag != null && blockTag.contains(bi.getBlock())) {
						if (en.getValue().overshadows(time)) {
							if (debug) System.out.println("Found a tag; it overshadows: "+en.getValue());
							time = en.getValue();
						}
					}
				}
			}
			if (stack.hasNbt()) {
				if (stack.hasCustomName() && LoaderItemDespawn.renamedDespawn.overshadows(time)) {
					if (debug) System.out.println("Item is renamed; renamedDespawn overshadows: "+LoaderItemDespawn.renamedDespawn);
					time = LoaderItemDespawn.renamedDespawn;
				}
				for (Map.Entry<String, ParsedTime> en : LoaderItemDespawn.nbtBools.entrySet()) {
					if (stack.getNbt().getBoolean(en.getKey())) {
						if (en.getValue().overshadows(time)) {
							if (debug) System.out.println("Found an NBT tag; it overshadows: "+en.getValue());
							time = en.getValue();
						}
					}
				}
			}
		}
		if (fabrication$fromPlayerDeath && LoaderItemDespawn.playerDeathDespawn.overshadows(time)) {
			if (debug) System.out.println("Item is from player death; playerDeathDespawn overshadows: "+LoaderItemDespawn.playerDeathDespawn);
			time = LoaderItemDespawn.playerDeathDespawn;
		}
		if (time == ParsedTime.UNSET) {
			if (debug) System.out.println("Time is unset, using default");
			time = thrower == null ? LoaderItemDespawn.dropsDespawn : LoaderItemDespawn.defaultDespawn;
		}
		if (debug) System.out.println("Final time: "+time);
		fabrication$invincible = false;
		if (time == ParsedTime.FOREVER) {
			fabrication$extraTime = 0;
			itemAge = -32768;
		} else if (time == ParsedTime.INVINCIBLE) {
			fabrication$extraTime = 0;
			itemAge = -32768;
			fabrication$invincible = true;
		} else if (time == ParsedTime.INSTANTLY) {
			remove(RemovalReason.DISCARDED);
		} else if (time == ParsedTime.UNSET) {
			fabrication$extraTime = 0;
		} else {
			int extra = time.timeInTicks-6000;
			extra -= Ints.saturatedCast(fabrication$trueAge);
			if (extra < 0) {
				itemAge = -extra;
				fabrication$extraTime = 0;
			} else {
				itemAge = 0;
				fabrication$extraTime = extra;
			}
		}
	}
	
	@Inject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
		if (fabrication$extraTime > 0) tag.putInt("fabrication:ExtraTime", fabrication$extraTime);
		tag.putLong("fabrication:TrueAge", fabrication$trueAge);
		if (fabrication$fromPlayerDeath) tag.putBoolean("fabrication:FromPlayerDeath", true);
		if (fabrication$invincible) tag.putBoolean("fabrication:Invincible", true);
	}
	
	@Inject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
		fabrication$extraTime = tag.getInt("fabrication:ExtraTime");
		fabrication$trueAge = tag.getLong("fabrication:TrueAge");
		fabrication$fromPlayerDeath = tag.getBoolean("fabrication:FromPlayerDeath");
		fabrication$invincible = tag.getBoolean("fabrication:Invincible");
	}
	
}