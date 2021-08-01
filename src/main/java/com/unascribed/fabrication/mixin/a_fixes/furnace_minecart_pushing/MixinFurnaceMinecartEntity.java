package com.unascribed.fabrication.mixin.a_fixes.furnace_minecart_pushing;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.unascribed.fabrication.interfaces.WasShoved;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Mixin(FurnaceMinecartEntity.class)
@EligibleIf(configAvailable="*.furnace_minecart_pushing")
public abstract class MixinFurnaceMinecartEntity extends AbstractMinecartEntity implements WasShoved {

	protected MixinFurnaceMinecartEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Shadow @Final
	private static Ingredient ACCEPTABLE_FUEL;
	@Shadow
	private int fuel;
	
	private boolean fabrication$wasShoved;
	private boolean fabrication$wasJustShoved;
	
	@Inject(at=@At("HEAD"), method="interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;")
	public void interactHead(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
		if (!MixinConfigPlugin.isEnabled("*.furnace_minecart_pushing")) return;
		ItemStack itemStack = player.getStackInHand(hand);
		fabrication$wasJustShoved = false;
		if (!FurnaceBlockEntity.canUseAsFuel(itemStack) && this.fuel == 0) {
			this.fuel = 1;
			fabrication$wasJustShoved = true;
		}
	}
	
	@Inject(at=@At("RETURN"), method="interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;")
	public void interactReturn(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
		if (!MixinConfigPlugin.isEnabled("*.furnace_minecart_pushing")) return;
		if (this.fuel > 0) {
			fabrication$wasShoved = fabrication$wasJustShoved;
		}
	}
	
	@Override
	public boolean fabrication$wasShoved() {
		return fabrication$wasShoved;
	}
	
}
