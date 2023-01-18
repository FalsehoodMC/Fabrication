package com.unascribed.fabrication.mixin.i_woina.flat_items;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.client.FlatItems;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.injection.FabModifyVariable;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemRenderer.class)
@EligibleIf(configAvailable="*.flat_items", envMatches=Env.CLIENT)
public class MixinHeldItemRenderer {

	@FabModifyVariable(at=@At("HEAD"), method="renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
			index=3, argsOnly=true)
	public Mode renderItemTransformMode(Mode orig, LivingEntity entity, ItemStack stack, Mode orig2, boolean leftHanded, MatrixStack matrices) {
		if (FabConf.isEnabled("*.flat_items")) {
			if (FlatItems.hasGeneratedModel(stack)) {
				if (orig == Mode.FIRST_PERSON_LEFT_HAND || orig == Mode.FIRST_PERSON_RIGHT_HAND) {
					matrices.translate(leftHanded ? -0.1 : 0.1, -0.16, -0.15);
					// multiply the model matrix directly to avoid corrupting normals
					matrices.peek().getPositionMatrix().scale(1, 1, 0);
					if (leftHanded) {
						matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
					}
					return Mode.GROUND;
				}
			}
		}
		return orig;
	}

}
