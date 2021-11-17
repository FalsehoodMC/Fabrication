package com.unascribed.fabrication.mixin.b_utility.canhit;

import java.util.List;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Mixin(ItemStack.class)
@EligibleIf(configEnabled="*.canhit", envMatches=Env.CLIENT)
public class MixinItemStackClient {

	@Inject(at=@At(value="INVOKE", target="net/minecraft/client/item/TooltipContext.isAdvanced()Z", ordinal=2),
			method="getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;",
			locals=LocalCapture.CAPTURE_FAILHARD)
	public void getTooltip(PlayerEntity player, TooltipContext ctx, CallbackInfoReturnable<List<Text>> ci, List<Text> list) {
		if (!MixinConfigPlugin.isEnabled("*.canhit")) return;
		ItemStack self = (ItemStack)(Object)this;
		if (self.hasTag() && self.getTag().contains("CanHit", NbtType.LIST) && !self.getTag().getBoolean("HideCanHit")) {
			list.add(LiteralText.EMPTY);
			list.add(new LiteralText("Can hit:").formatted(Formatting.GRAY));
			NbtList canhit = self.getTag().getList("CanHit", NbtType.STRING);
			if (canhit.isEmpty()) {
				list.add(new LiteralText("Nothing").formatted(Formatting.GRAY));
			}
			for (int i = 0; i < canhit.size(); i++) {
				String s = canhit.getString(i);
				if (s.contains("-")) {
					try {
						UUID.fromString(s);
						list.add(new LiteralText(s).formatted(Formatting.DARK_GRAY));
						continue;
					} catch (IllegalArgumentException ex) {}
				}
				if (s.startsWith("@")) {
					// TODO parse and format complex selectors? (oh god)
					list.add(new LiteralText(s).formatted(Formatting.DARK_GRAY));
				} else {
					boolean negated = false;
					if (s.startsWith("!")) {
						negated = true;
						s = s.substring(1);
					}
					final String id = s.contains(":") ? s : "minecraft:"+s;
					EntityType<?> type = EntityType.get(id).orElse(null);
					if (type == null) {
						list.add(new LiteralText("missingno").formatted(Formatting.DARK_GRAY));
					} else {
						list.add(new LiteralText(negated ? "Not " : "").formatted(Formatting.DARK_GRAY).append(type.getName()));
					}
				}
			}
		}
	}
	
	
}
