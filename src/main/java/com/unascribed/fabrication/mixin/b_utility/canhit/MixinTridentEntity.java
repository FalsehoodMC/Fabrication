package com.unascribed.fabrication.mixin.b_utility.canhit;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;

import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;

@Mixin(TridentEntity.class)
@EligibleIf(configAvailable="*.canhit")
public abstract class MixinTridentEntity extends PersistentProjectileEntity implements SetCanHitList {

	protected MixinTridentEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public NbtList fabrication$getCanHitList() {
		ItemStack tridentStack = getItemStack();
		return tridentStack.contains(DataComponentTypes.CUSTOM_DATA) && tridentStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().contains("CanHit") && !CanHitUtil.isExempt(this.getOwner()) ?
				tridentStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).getNbt().getList("CanHit", NbtElement.STRING_TYPE) : null;
	}

}
