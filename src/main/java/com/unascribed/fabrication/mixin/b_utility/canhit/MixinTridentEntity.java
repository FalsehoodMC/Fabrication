package com.unascribed.fabrication.mixin.b_utility.canhit;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;

@Mixin(TridentEntity.class)
@EligibleIf(configAvailable="*.canhit")
public abstract class MixinTridentEntity extends PersistentProjectileEntity implements SetCanHitList {

	protected MixinTridentEntity(EntityType<? extends PersistentProjectileEntity> type, World world, ItemStack stack) {
		super(type, world, stack);
	}

	@Override
	public NbtList fabrication$getCanHitList() {
		ItemStack tridentStack = getItemStack();
		return tridentStack.hasNbt() && tridentStack.getNbt().contains("CanHit") && !CanHitUtil.isExempt(((TridentEntity)(Object)this).getOwner()) ?
				tridentStack.getNbt().getList("CanHit", NbtType.STRING) : null;
	}

}
