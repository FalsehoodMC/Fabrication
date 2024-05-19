package com.unascribed.fabrication.mixin.d_minor_mechanics.furnace_minecart_resupplying;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.ResupplyingFurnaceCart;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Predicate;

@Mixin(HopperBlockEntity.class)
@EligibleIf(configAvailable="*.furnace_minecart_resupplying")
public class MixinHopperBlockEntity {

	@FabModifyArg(method="getInventoryAt(Lnet/minecraft/world/World;DDD)Lnet/minecraft/inventory/Inventory;", at=@At(value="INVOKE", target="Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"))
	private static <T extends Entity> Predicate<T> addFurnaceCarts(Predicate<T> predicate) {
		if (!FabConf.isEnabled("*.furnace_minecart_resupplying")) return predicate;
		return predicate.or(new Predicate<>() {
			@Override
			public boolean test(T entity) {
				return entity instanceof ResupplyingFurnaceCart;
			}
		});
	}
	@ModifyReturn(method="getInventoryAt(Lnet/minecraft/world/World;DDD)Lnet/minecraft/inventory/Inventory;", target="Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;")
	private static List<Entity> fabrication$changeFurnaceCarts(List<Entity> list) {
		for (int i=0, l=list.size(); i<l; i++) {
			Entity entity = list.get(i);
			if (entity instanceof ResupplyingFurnaceCart) {
				list.set(i, ((ResupplyingFurnaceCart) entity).fabrication$getResupplyingFurnaceCart());
			}
		}
		return list;
	}

}
