package com.unascribed.fabrication.mixin.b_utility.killmessage;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.GetKillMessage;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin.RuntimeChecks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

@Mixin({EntityDamageSource.class, ProjectileDamageSource.class})
@EligibleIf(configEnabled="*.killmessage")
public abstract class MixinEntityDamageSource {

	@Unique
	private static final Pattern fabrication$placeholderPattern = Pattern.compile("(?<!%)%(?:([123])\\$)?s");
	
	@Inject(at=@At("HEAD"), method="getDeathMessage(Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/text/Text;", cancellable=true)
	public void getDeathMessage(LivingEntity victim, CallbackInfoReturnable<Text> rtrn) {
		if (!RuntimeChecks.check("*.killmessage")) return;
		Entity attacker = ((EntityDamageSource)(Object)this).getAttacker();
		if (attacker instanceof GetKillMessage) {
			String msg = ((GetKillMessage)attacker).fabrication$getKillMessage();
			System.out.println(msg);
			if (msg != null) {
				Matcher m = fabrication$placeholderPattern.matcher(msg);
				if (m.find()) {
					m.reset(msg);
					LiteralText base = new LiteralText("");
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
							Iterator<ItemStack> iter = attacker.getItemsHand().iterator();
							if (iter.hasNext()) {
								ItemStack held = iter.next();
								base.append(held.toHoverableText());
							} else {
								base.append(m.group());
							}
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
					rtrn.setReturnValue(new LiteralText(msg));
				}
			}
		}
	}
	
}
