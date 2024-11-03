package com.unascribed.fabrication.mixin.d_minor_mechanics.infibows;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.util.EnchantmentHelperHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
@EligibleIf(configAvailable="*.infibows")
public abstract class MixinPlayerEntity extends LivingEntity {

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@FabInject(at=@At(value="FIELD", target="Lnet/minecraft/entity/player/PlayerAbilities;creativeMode:Z"), method="getProjectileType(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", cancellable=true)
	private void infiBow(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
		if (FabConf.isEnabled("*.infibows") && EnchantmentHelperHelper.getLevel(this.getWorld().getRegistryManager(), Enchantments.INFINITY, stack) > 0)
			cir.setReturnValue(new ItemStack(Items.ARROW));
	}

}
