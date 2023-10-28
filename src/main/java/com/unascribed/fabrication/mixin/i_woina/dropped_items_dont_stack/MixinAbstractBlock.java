package com.unascribed.fabrication.mixin.i_woina.dropped_items_dont_stack;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractBlock.class)
@EligibleIf(configAvailable="*.dropped_items_dont_stack")
public abstract class MixinAbstractBlock {

	@ModifyReturn(method="getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/loot/context/LootContext$Builder;)Ljava/util/List;", target="Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;)Ljava/util/List;")
	private static List<ItemStack> splitLoot(List<ItemStack> inp) {
		if(!FabConf.isEnabled("*.dropped_items_dont_stack") || inp == null) return inp;
		List<ItemStack> ret = new ArrayList<>();
		for (ItemStack stack : inp) {
			ItemStack single = stack.copy();
			single.setCount(1);
			for (int i = 0; i < stack.getCount()-1; i++) {
				ret.add(single.copy());
			}
			ret.add(single);
		}
		return ret;
	}

}
