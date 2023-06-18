package com.unascribed.fabrication.mixin.i_woina.dropped_items_dont_stack;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(value=LivingEntity.class, priority=999)
@EligibleIf(configAvailable="*.dropped_items_dont_stack")
public abstract class MixinLivingEntity {

	@FabModifyArg(method="dropLoot(Lnet/minecraft/entity/damage/DamageSource;Z)V", at=@At(value="INVOKE", target="Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContextParameterSet;JLjava/util/function/Consumer;)V"))
	public Consumer<ItemStack> splitLoot(Consumer<ItemStack> lootConsumer) {
		if(!FabConf.isEnabled("*.dropped_items_dont_stack")) return lootConsumer;
		return stack ->{
			ItemStack single = stack.copy();
			single.setCount(1);
			for (int i=0; i<stack.getCount(); i++){
				lootConsumer.accept(single);
			}
		};

	}

}
