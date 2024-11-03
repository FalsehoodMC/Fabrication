package com.unascribed.fabrication.mixin.d_minor_mechanics.furnace_minecart_resupplying;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.ResupplyingFurnaceCart;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.FabModifyArg;
import com.unascribed.fabrication.util.forgery_nonsense.ForgeryFurnaceCartResupplying;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(HopperBlockEntity.class)
@EligibleIf(configAvailable="*.furnace_minecart_resupplying")
public class MixinHopperBlockEntity {


	@FabModifyArg(method="getEntityInventoryAt(Lnet/minecraft/world/World;DDD)Lnet/minecraft/inventory/Inventory;", at=@At(value="INVOKE", target="Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"))
	private static Predicate addFurnaceCarts(Predicate predicate) {
		if (!FabConf.isEnabled("*.furnace_minecart_resupplying")) return predicate;
		return ForgeryFurnaceCartResupplying.INSTANCE.and(predicate);
	}
	@FabInject(method="getEntityInventoryAt(Lnet/minecraft/world/World;DDD)Lnet/minecraft/inventory/Inventory;", at=@At(value="INVOKE", target="Ljava/util/List;isEmpty()Z"), cancellable=true)
	private static void checkFurnaceCarts(World world, double x, double y, double z, CallbackInfoReturnable<Inventory> cir) {
		List<ResupplyingFurnaceCart> furnaceList = ForgeryFurnaceCartResupplying.fabrication$fmr$lastCart.get();
		if (furnaceList == null) return;
		int i = 0;
		while (i<furnaceList.size()) {
			Vec3d pos = ((Entity)furnaceList.get(i)).getPos();
			if (Math.abs(x-pos.getX())>1 ||Math.abs(y-pos.getY())>1 ||Math.abs(z-pos.getZ())>1) {
				furnaceList.remove(i);
			} else {
				i++;
			}
		}
		if (!furnaceList.isEmpty()) {
			i = world.random.nextInt(furnaceList.size()+1);
			if (i != furnaceList.size()) {
				cir.setReturnValue(furnaceList.get(i).fabrication$getResupplyingFurnaceCart());
			}
		}
		ForgeryFurnaceCartResupplying.fabrication$fmr$lastCart.remove();
	}

}
