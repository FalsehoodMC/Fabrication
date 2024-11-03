package com.unascribed.fabrication.mixin.a_fixes.fix_dragon_egg_trails;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.Hijack;
import com.unascribed.fabrication.support.injection.HijackReturn;
import com.unascribed.fabrication.util.ByteBufCustomPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DragonEggBlock.class)
@EligibleIf(configAvailable="*.fix_dragon_egg_trails")
@FailOn(invertedSpecialConditions=SpecialEligibility.NOT_FORGE)
public class MixinDragonEggBlock {

	@Hijack(method="teleport(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", target="Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z")
	private static HijackReturn fabrication$fixEggTrails$giveClientAccuratePos(World world1, BlockPos newPos, BlockState state, int flag, DragonEggBlock self, BlockState initState, World world, BlockPos pos) {
		if (!world.isClient && FabConf.isEnabled("*.fix_dragon_egg_trails")) {
			for (PlayerEntity ent : world.getPlayers()) {
				if (ent.getBlockPos().getManhattanDistance(pos) > 128) return null;
				if (ent instanceof SetFabricationConfigAware && ent instanceof ServerPlayerEntity && ((SetFabricationConfigAware)ent).fabrication$getReqVer() >= 0) {
					PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
					data.writeBlockPos(pos);
					data.writeBlockPos(newPos);
					CustomPayloadS2CPacket pkt = new CustomPayloadS2CPacket(new ByteBufCustomPayload(Identifier.of("fabrication", "dragon_egg_trail"), data));
					((ServerPlayerEntity)ent).networkHandler.sendPacket(pkt);
				}
			}
		}
		return null;
	}
}
