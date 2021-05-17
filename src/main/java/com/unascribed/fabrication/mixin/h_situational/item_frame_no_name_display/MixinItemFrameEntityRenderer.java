package com.unascribed.fabrication.mixin.h_situational.item_frame_no_name_display;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.tree.BeehiveTreeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;
import java.util.Set;

@Mixin(value=ItemFrameEntityRenderer.class, priority=1004)
@EligibleIf(configEnabled="*.item_frame_no_name_display", envMatches = Env.CLIENT)
public class MixinItemFrameEntityRenderer {

	@Inject(at=@At("HEAD"), method= "hasLabel(Lnet/minecraft/entity/decoration/ItemFrameEntity;)Z", cancellable=true)
	public void hasLabel(ItemFrameEntity itemFrameEntity, CallbackInfoReturnable<Boolean> cir) {
		if (MixinConfigPlugin.isEnabled("*.item_frame_no_name_display")) {
			cir.setReturnValue(false);
		}
	}
	
}
