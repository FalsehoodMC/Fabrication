package com.unascribed.fabrication.mixin.f_balance.pickup_skeleton_arrows;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.TagStrayArrow;
import com.unascribed.fabrication.support.EligibleIf;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArrowEntity.class)
@EligibleIf(configAvailable="*.pickup_skeleton_arrows")
public abstract class MixinArrowEntity implements TagStrayArrow {

	boolean fabrication$firedByStray = false;

	@FabInject(at=@At("RETURN"), method="asItemStack()Lnet/minecraft/item/ItemStack;")
	protected void asItemStack(CallbackInfoReturnable<ItemStack> cir) {
		if (!(FabConf.isEnabled("*.pickup_skeleton_arrows") && fabrication$firedByStray)) return;
		ItemStack arrow = cir.getReturnValue();
		if (!arrow.hasCustomName())
			arrow.setCustomName(new TranslatableText("item.minecraft.tipped_arrow.effect.slowness"));
		if (!(arrow.hasTag() && arrow.getTag().contains("CustomPotionColor")))
			arrow.getOrCreateTag().putInt("CustomPotionColor", PotionUtil.getColor(Potions.SLOWNESS));
	}

	@FabInject(at=@At("HEAD"), method="writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
		nbt.putBoolean("fabrication$firedByStray", fabrication$firedByStray);
	}

	@FabInject(at=@At("HEAD"), method="readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V")
	public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		fabrication$firedByStray = nbt.getBoolean("fabrication$firedByStray");
	}

	@Override
	public void fabrication$firedByStray() {
		fabrication$firedByStray = true;
	}

}
