package com.unascribed.fabrication.mixin.b_utility.item_despawn;

import com.google.common.primitives.Ints;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.ItemDespawn;
import com.unascribed.fabrication.interfaces.SetFromPlayerDeath;
import com.unascribed.fabrication.loaders.LoaderItemDespawn;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.ModifyGetField;
import com.unascribed.fabrication.util.ParsedTime;
import com.unascribed.fabrication.util.Resolvable;
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
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(ItemEntity.class)
@EligibleIf(configAvailable="*.item_despawn")
public abstract class MixinItemEntity extends Entity implements SetFromPlayerDeath, ItemDespawn {

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

	@FabInject(at=@At("HEAD"), method="tick()V")
	public void tickHead(CallbackInfo ci) {
		if (fabrication$extraTime > 0) {
			fabrication$extraTime--;
			itemAge--;
		}
		fabrication$trueAge++;
		int worldBottom = world.getBottomY();
		if (getPos().y < worldBottom-32) {
			if (fabrication$invincible) {
				teleport(getPos().x, worldBottom+1, getPos().z);
				setVelocity(0,0,0);
				if (!world.isClient) {
					((ServerWorld)world).getChunkManager().sendToNearbyPlayers(this, new EntityPositionS2CPacket(this));
					((ServerWorld)world).getChunkManager().sendToNearbyPlayers(this, new EntityVelocityUpdateS2CPacket(this));
				}
			}
		}
	}

	@FabInject(at=@At("HEAD"), method="damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", cancellable=true)
	public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
		if (fabrication$invincible || (FabConf.isEnabled("*.item_despawn") && world.isClient)) {
			ci.setReturnValue(false);
		}
	}

	@FabInject(at=@At("TAIL"), method="setStack(Lnet/minecraft/item/ItemStack;)V")
	public void setStack(ItemStack stack, CallbackInfo ci) {
		calculateDespawn();
	}

	@FabInject(at=@At("TAIL"), method="setThrower(Ljava/util/UUID;)V")
	public void setThrower(UUID id, CallbackInfo ci) {
		calculateDespawn();
	}

	@ModifyGetField(target="net/minecraft/entity/ItemEntity.itemAge:I", method="canMerge()Z")
	private static int fabrication$modifyIllegalAge(int orig, ItemEntity item) {
		// age-1 will never be equal to age; short-circuits the "age != -32768" check and allows
		// items set to "invincible" to stack together
		return item instanceof ItemDespawn && ((ItemDespawn) item).fabrication$itemDespawn$invinc() ? orig -1 : orig;
	}
	public boolean fabrication$itemDespawn$invinc() {
		return fabrication$invincible;
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
		ParsedTime time = LoaderItemDespawn.itemDespawns.get(Resolvable.mapKey(stack.getItem(), Registry.ITEM));
		if (debug) System.out.println("itemTime: "+time);
		if (time == null) {
			time = ParsedTime.Unset.NORMAL;
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
				TagKey<Item> itemTag = TagKey.of(Registry.ITEM_KEY, en.getKey());
				if (stack.isIn(itemTag)) {
					if (en.getValue().overshadows(time)) {
						if (debug) System.out.println("Found a tag; it overshadows: "+en.getValue());
						time = en.getValue();
					}
				}
				if (stack.getItem() instanceof BlockItem) {
					BlockItem bi = (BlockItem)stack.getItem();
					TagKey<Block> blockTag = TagKey.of(Registry.BLOCK_KEY, en.getKey());
					if (bi.getBlock().getRegistryEntry().isIn(blockTag)) {
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
		if (time instanceof ParsedTime.Unset) {
			if (debug) System.out.println("Time is unset, using default");
			time = thrower == null ? LoaderItemDespawn.dropsDespawn : LoaderItemDespawn.defaultDespawn;
		}
		if (debug) System.out.println("Final time: "+time);
		fabrication$invincible = false;
		if (time instanceof ParsedTime.Forever) {
			fabrication$extraTime = 0;
			itemAge = -32768;
		} else if (time instanceof ParsedTime.Invincible) {
			fabrication$extraTime = 0;
			itemAge = -32768;
			fabrication$invincible = true;
		} else if (time instanceof ParsedTime.Instant) {
			discard();
		} else if (time instanceof ParsedTime.Unset) {
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

	@FabInject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
		if (fabrication$extraTime > 0) tag.putInt("fabrication:ExtraTime", fabrication$extraTime);
		tag.putLong("fabrication:TrueAge", fabrication$trueAge);
		if (fabrication$fromPlayerDeath) tag.putBoolean("fabrication:FromPlayerDeath", true);
		if (fabrication$invincible) tag.putBoolean("fabrication:Invincible", true);
	}

	@FabInject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
		fabrication$extraTime = tag.getInt("fabrication:ExtraTime");
		fabrication$trueAge = tag.getLong("fabrication:TrueAge");
		fabrication$fromPlayerDeath = tag.getBoolean("fabrication:FromPlayerDeath");
		fabrication$invincible = tag.getBoolean("fabrication:Invincible");
	}

}
