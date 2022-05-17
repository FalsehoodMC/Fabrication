package com.unascribed.fabrication.mixin.b_utility.taggable_players;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.features.FeatureTaggablePlayers;
import com.unascribed.fabrication.interfaces.TaggablePlayer;
import com.unascribed.fabrication.loaders.LoaderTaggablePlayers;
import com.unascribed.fabrication.support.EligibleIf;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
@EligibleIf(configAvailable="*.taggable_players")
public abstract class MixinServerPlayerEntity extends PlayerEntity implements TaggablePlayer {

	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}

	private final Set<String> fabrication$tags = new HashSet<>();

	@Override
	public Set<String> fabrication$getTags() {
		return ImmutableSet.copyOf(fabrication$tags);
	}

	@Override
	public void fabrication$clearTags() {
		fabrication$tags.clear();
	}

	@Override
	public void fabrication$setTag(String tag, boolean enabled) {
		if (enabled) {
			fabrication$tags.add(tag);
		} else {
			fabrication$tags.remove(tag);
		}
	}

	@Override
	public boolean fabrication$hasTag(String tag) {
		return fabrication$tags.contains(tag);
	}

	@Inject(at=@At("HEAD"), method="copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V")
	public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
		fabrication$tags.clear();
		fabrication$tags.addAll(((TaggablePlayer)oldPlayer).fabrication$getTags());
	}

	@Inject(at=@At("TAIL"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
		NbtList li = new NbtList();
		for (String pt : fabrication$tags) {
			li.add(NbtString.of(pt));
		}
		if (!li.isEmpty()) {
			tag.put("fabrication:Tags", li);
		}
	}

	@Inject(at=@At("TAIL"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
		fabrication$tags.clear();
		NbtList li = tag.getList("fabrication:Tags", NbtType.STRING);
		for (int i = 0; i < li.size(); i++) {
			String key = li.getString(i);
			String fullKey = FabConf.remap("*."+key.toLowerCase(Locale.ROOT));
			if (!FeatureTaggablePlayers.activeTags.containsKey(fullKey)) {
				FabLog.warn("TaggablePlayers added "+fullKey+" as a valid option because a player was tagged with it");
				FeatureTaggablePlayers.add(fullKey, 0);
			}
			fabrication$tags.add(key);
		}
	}

}
