package com.unascribed.fabrication.mixin.b_utility.despawning_items_blink;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.interfaces.RenderingAgeAccess;
import com.unascribed.fabrication.interfaces.SetAttackerYawAware;
import com.unascribed.fabrication.support.EligibleIf;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

@Mixin(ItemEntity.class)
@EligibleIf(configAvailable="*.despawning_items_blink")
public abstract class MixinItemEntity extends Entity implements RenderingAgeAccess {

	public MixinItemEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private static final Identifier FABRICATION$ITEM_DESPAWN = new Identifier("fabrication", "item_despawn");

	@Shadow
	private int itemAge;

	private int fabrication$renderingAge = -1000000;

	@Inject(at=@At("TAIL"), method="tick()V")
	public void tick(CallbackInfo ci) {
		if (!world.isClient) {
			if (age % 10 == 0) {
				PacketByteBuf data = new PacketByteBuf(Unpooled.buffer(8));
				data.writeInt(getId());
				data.writeInt(itemAge);
				FabricationMod.sendToTrackersMatching(this, new CustomPayloadS2CPacket(FABRICATION$ITEM_DESPAWN, data), spe -> spe instanceof SetAttackerYawAware && ((SetAttackerYawAware) spe).fabrication$isAttackerYawAware());
			}
		}
		fabrication$renderingAge++;
	}

	@Override
	public int fabrication$getRenderingAge() {
		return fabrication$renderingAge;
	}

	@Override
	public void fabrication$setRenderingAge(int age) {
		fabrication$renderingAge = age;
	}

}
