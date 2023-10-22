package com.unascribed.fabrication.mixin.z_combined.spiders_cant_climb;

import com.unascribed.fabrication.FabConf;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.block.BlockState;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.world.World;

@Mixin(SpiderEntity.class)
@EligibleIf(anyConfigAvailable={"*.spiders_cant_climb_glazed_terracotta", "*.spiders_cant_climb_while_wet"})
public abstract class MixinSpiderEntity extends HostileEntity {
	private static final TagKey<Block> fabrication$spidersCantClimb = TagKey.of(RegistryKeys.BLOCK, new Identifier("fabrication", "spiders_cant_climb"));

	protected MixinSpiderEntity(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	public abstract void setClimbingWall(boolean b);

	@Shadow
	public abstract boolean isClimbingWall();

	@FabInject(at=@At("TAIL"), method="tick()V")
	public void tickTail(CallbackInfo ci) {
		if (!getWorld().isClient) {
			if (isClimbingWall()) {
				if (FabConf.isEnabled("*.spiders_cant_climb_while_wet") && isWet()) {
					setClimbingWall(false);
				} else if (FabConf.isEnabled("*.spiders_cant_climb_glazed_terracotta")) {
					// :(
					// this used to be done by shrinking the terracotta hitbox and using
					// onEntityCollision, but that makes spiders get caught on things...
					FabricationMod.forAllAdjacentBlocks(this, (w, bp, bp2, dir) -> {
						BlockState bs = getWorld().getBlockState(bp);
						if (bs.getBlock() instanceof GlazedTerracottaBlock || bs.isIn(fabrication$spidersCantClimb)) {
							setClimbingWall(false);
							return false;
						}
						return true;
					});
				}
			}
		}
	}

}
