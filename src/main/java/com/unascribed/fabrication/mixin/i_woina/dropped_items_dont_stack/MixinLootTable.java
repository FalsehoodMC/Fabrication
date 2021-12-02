package com.unascribed.fabrication.mixin.i_woina.dropped_items_dont_stack;

import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(LootTable.class)
@EligibleIf(configAvailable="*.dropped_items_dont_stack")
public abstract class MixinLootTable {

	@Inject(at=@At("HEAD"), method="processStacks(Ljava/util/function/Consumer;)Ljava/util/function/Consumer;", cancellable=true)
	private static void splitStacks(Consumer<ItemStack> lootConsumer, CallbackInfoReturnable<Consumer<ItemStack>> cir) {
		if (!MixinConfigPlugin.isEnabled("*.dropped_items_dont_stack")) return;
		cir.setReturnValue(stack ->{
			ItemStack single = stack.copy();
			single.setCount(1);
			for (int i=0; i<stack.getCount(); i++){
				lootConsumer.accept(single);
			}
		});
	}

}
