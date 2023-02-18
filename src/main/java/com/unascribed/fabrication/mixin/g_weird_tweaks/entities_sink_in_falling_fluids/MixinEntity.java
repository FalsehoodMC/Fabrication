package com.unascribed.fabrication.mixin.g_weird_tweaks.entities_sink_in_falling_fluids;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(Entity.class)
@EligibleIf(configAvailable="*.entities_sink_in_falling_fluids")
public class MixinEntity {

	private static final Predicate<LivingEntity> fabrication$entitiesCantSwimUpstream = ConfigPredicates.getFinalPredicate("*.entities_sink_in_falling_fluids");
	private boolean fabrication$inUpstreamFluid = false;
	@ModifyReturn(method="updateMovementInFluid(Lnet/minecraft/tag/TagKey;D)Z", target="Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;")
	private FluidState fabrication$getUpstream(FluidState state) {
		if (FabConf.isEnabled("*.entities_sink_in_falling_fluids")) {
			Object self = this;
			if (self instanceof LivingEntity && fabrication$entitiesCantSwimUpstream.test((LivingEntity)self)) {
				fabrication$inUpstreamFluid = state.getOrEmpty(FlowableFluid.FALLING).orElse(false);
				return state;
			}
		}
		if (fabrication$inUpstreamFluid) fabrication$inUpstreamFluid = false;
		return state;
	}
	@FabModifyArg(method="updateMovementInFluid(Lnet/minecraft/tag/TagKey;D)Z",
			  at=@At(value="INVOKE", target="Lnet/minecraft/util/math/Vec3d;add(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", ordinal=0))
	private Vec3d fabrication$disableUpstream(Vec3d vec) {
		if (fabrication$inUpstreamFluid) {
			return new Vec3d(vec.x, vec.y-9, vec.z);
		}
		return vec;
	}
}
