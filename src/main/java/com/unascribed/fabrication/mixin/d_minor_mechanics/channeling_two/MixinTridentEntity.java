package com.unascribed.fabrication.mixin.d_minor_mechanics.channeling_two;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TridentEntity.class)
@EligibleIf(configAvailable="*.channeling_two")
abstract public class MixinTridentEntity extends PersistentProjectileEntity {

	@Shadow
	private ItemStack tridentStack;

	protected MixinTridentEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@ModifyReturn(method="onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V", target="Lnet/minecraft/world/World;isThundering()Z")
	public boolean fabrication$channellingTwo(boolean bool) {
		if (!(FabConf.isEnabled("*.channeling_two") && EnchantmentHelper.getLevel(Enchantments.CHANNELING, this.tridentStack) > 1)) return bool;
		return this.getWorld().hasRain(this.getBlockPos());
	}

}
