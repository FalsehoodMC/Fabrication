package com.unascribed.fabrication.mixin.i_woina.dropped_items_dont_stack;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
@EligibleIf(configAvailable="*.dropped_items_dont_stack")
public abstract class MixinLootTable {

	@ModifyArg(method="dropLoot(Lnet/minecraft/entity/damage/DamageSource;Z)V", at=@At(value="INVOKE", target="Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V"))
	public Consumer<ItemStack> generateLoot(Consumer<ItemStack> lootConsumer) {
		if(!MixinConfigPlugin.isEnabled("*.dropped_items_dont_stack")) return lootConsumer;
		return stack ->{
			ItemStack single = stack.copy();
			single.setCount(1);
			for (int i=0; i<stack.getCount(); i++){
				lootConsumer.accept(single);
			}
		};

	}

}
