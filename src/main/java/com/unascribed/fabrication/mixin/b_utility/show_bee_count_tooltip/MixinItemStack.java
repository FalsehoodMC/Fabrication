package com.unascribed.fabrication.mixin.b_utility.show_bee_count_tooltip;

import java.util.List;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
@EligibleIf(anyConfigAvailable={"*.show_bee_count_tooltip"}, envMatches=Env.CLIENT)
public class MixinItemStack {

	@FabInject(at=@At("RETURN"), method="getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;", cancellable=true)
	public void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
		ItemStack stack = (ItemStack)(Object)this;
		if (!(FabConf.isEnabled("*.show_bee_count_tooltip") && stack.hasTag())) return;
		NbtCompound tag = stack.getTag().getCompound("BlockEntityTag");
		if (tag == null || !tag.contains("Bees", NbtType.LIST)) return;

		List<Text> list = cir.getReturnValue();
		list.add(new LiteralText("Bees: " + ((NbtList) tag.get("Bees")).size()));
		cir.setReturnValue(list);
	}
}
