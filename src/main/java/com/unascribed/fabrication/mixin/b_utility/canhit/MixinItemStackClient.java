package com.unascribed.fabrication.mixin.b_utility.canhit;

import java.util.List;
import java.util.UUID;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.injection.FabInject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(ItemStack.class)
@EligibleIf(configAvailable="*.canhit", envMatches=Env.CLIENT)
public class MixinItemStackClient {

	@FabInject(at=@At(value="INVOKE", target="net/minecraft/client/item/TooltipContext.isAdvanced()Z", ordinal=2),
			method="getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;",
			locals=LocalCapture.CAPTURE_FAILHARD)
	public void getTooltip(PlayerEntity player, TooltipContext ctx, CallbackInfoReturnable<List<Text>> ci, List<Text> list) {
		if (!FabConf.isEnabled("*.canhit")) return;
		ItemStack self = (ItemStack)(Object)this;
		if (self.hasNbt() && self.getNbt().contains("CanHit", NbtType.LIST) && !self.getNbt().getBoolean("HideCanHit")) {
			list.add(Text.empty());
			list.add(Text.literal("Can hit:").formatted(Formatting.GRAY));
			NbtList canhit = self.getNbt().getList("CanHit", NbtType.STRING);
			if (canhit.isEmpty()) {
				list.add(Text.literal("Nothing").formatted(Formatting.GRAY));
			}
			for (int i = 0; i < canhit.size(); i++) {
				String s = canhit.getString(i);
				if (s.contains("-")) {
					try {
						UUID.fromString(s);
						list.add(Text.literal(s).formatted(Formatting.DARK_GRAY));
						continue;
					} catch (IllegalArgumentException ex) {}
				}
				if (s.startsWith("@")) {
					// TODO parse and format complex selectors? (oh god)
					list.add(Text.literal(s).formatted(Formatting.DARK_GRAY));
				} else {
					boolean negated = false;
					if (s.startsWith("!")) {
						negated = true;
						s = s.substring(1);
					}
					final String id = s.contains(":") ? s : "minecraft:"+s;
					EntityType<?> type = EntityType.get(id).orElse(null);
					if (type == null) {
						list.add(Text.literal("missingno").formatted(Formatting.DARK_GRAY));
					} else {
						list.add(Text.literal(negated ? "Not " : "").formatted(Formatting.DARK_GRAY).append(type.getName()));
					}
				}
			}
		}
	}


}
