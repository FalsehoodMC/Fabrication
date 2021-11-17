package com.unascribed.fabrication.mixin.b_utility.canhit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.unascribed.fabrication.interfaces.SetCanHitList;
import com.unascribed.fabrication.logic.CanHitUtil;
import com.unascribed.fabrication.support.EligibleIf;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;

@Mixin(TridentEntity.class)
@EligibleIf(configEnabled="*.canhit")
public abstract class MixinTridentEntity implements SetCanHitList {

	@Shadow
	private ItemStack tridentStack;

	@Override
	public NbtList fabrication$getCanHitList() {
		return tridentStack.hasTag() && tridentStack.getTag().contains("CanHit") && !CanHitUtil.isExempt(((TridentEntity)(Object)this).getOwner()) ?
				tridentStack.getTag().getList("CanHit", NbtType.STRING) : null;
	}

}
