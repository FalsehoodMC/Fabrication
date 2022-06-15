package com.unascribed.fabrication.mixin.b_utility.killmessage;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.unascribed.fabrication.FabConf;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.GetKillMessage;
import com.unascribed.fabrication.support.EligibleIf;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Mixin({EntityDamageSource.class, ProjectileDamageSource.class})
@EligibleIf(configAvailable="*.killmessage")
public abstract class MixinEntityDamageSource {

	@Unique
	private static final Pattern fabrication$placeholderPattern = Pattern.compile("(?<!%)%(?:([123])\\$)?s");

	@Inject(at=@At("HEAD"), method="getDeathMessage(Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/text/Text;", cancellable=true)
	public void getDeathMessage(LivingEntity victim, CallbackInfoReturnable<Text> rtrn) {
		if (!FabConf.isEnabled("*.killmessage")) return;
		Entity attacker = ((EntityDamageSource)(Object)this).getAttacker();
		if (attacker instanceof GetKillMessage) {
			Iterator<ItemStack> iter = attacker.getItemsHand().iterator();
			ItemStack held;
			if (iter.hasNext()) {
				held = iter.next();
			} else {
				held = ItemStack.EMPTY;
			}
			String msg = null;
			if (held.hasNbt() && held.getNbt().contains("KillMessage", NbtType.STRING)) {
				msg = held.getNbt().getString("KillMessage");
			}
			if (msg == null) {
				msg = ((GetKillMessage)attacker).fabrication$getKillMessage();
			}
			if (msg != null) {
				Matcher m = fabrication$placeholderPattern.matcher(msg);
				if (m.find()) {
					m.reset(msg);
					MutableText base = Text.empty();
					int prev = 0;
					int defIdx = 0;
					while (m.find()) {
						if (prev < msg.length()) {
							base.append(msg.substring(prev, m.start()));
						}
						int idx = defIdx;
						if (m.group(1) != null) {
							idx = Integer.parseInt(m.group(1))-1;
						} else {
							defIdx++;
						}
						if (idx == 0) {
							base.append(victim.getDisplayName());
						} else if (idx == 1) {
							base.append(attacker.getDisplayName());
						} else if (idx == 2) {
							base.append(held.toHoverableText());
						} else {
							base.append(m.group());
						}
						prev = m.end();
					}
					if (prev < msg.length()) {
						base.append(msg.substring(prev));
					}
					rtrn.setReturnValue(base);
				} else {
					rtrn.setReturnValue(Text.literal(msg));
				}
			}
		}
	}

}
