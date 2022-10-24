package com.unascribed.fabrication.mixin.z_combined.old_armor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.injection.FabInject;
import com.unascribed.fabrication.support.injection.ModifyReturn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.AbstractMap;
import java.util.function.Predicate;
import java.util.stream.Collector;

@Mixin(LivingEntity.class)
@EligibleIf(anyConfigAvailable={"*.old_armor_scale", "*.old_armor"})
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Shadow
	protected abstract ItemStack getEquippedStack(EquipmentSlot slot);

	@Shadow
	public abstract AttributeContainer getAttributes();

	@FabInject(at=@At("HEAD"), method="applyArmorToDamage(Lnet/minecraft/entity/damage/DamageSource;F)F")
	public void refreshArmor(DamageSource source, float amount, CallbackInfoReturnable<Float> cir){
		for(EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() != EquipmentSlot.Type.ARMOR) continue;
			ItemStack itemStack3 = this.getEquippedStack(slot);
				if (!itemStack3.isEmpty()) {
					this.getAttributes().removeModifiers(fabrication$oldArmor(itemStack3.getAttributeModifiers(slot), itemStack3, slot, (LivingEntity)(Object)this));
					this.getAttributes().addTemporaryModifiers(fabrication$oldArmor(itemStack3.getAttributeModifiers(slot), itemStack3, slot, (LivingEntity)(Object)this));
				}
		}
	}

	//Purely for visuals
	@FabInject(at=@At("HEAD"), method="tick()V")
	public void tickArmor(CallbackInfo ci){
		if ((this.age & 16) == 0) refreshArmor(null, 0, null);
	}

	private static final Predicate<LivingEntity> fabrication$oldArmorScalePredicate = ConfigPredicates.getFinalPredicate("*.old_armor_scale");
	private static final Predicate<LivingEntity> fabrication$oldArmorPredicate = ConfigPredicates.getFinalPredicate("*.old_armor");
	@ModifyReturn(target="Lnet/minecraft/item/ItemStack;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;",
			method="method_30129()Ljava/util/Map;")
	private static Multimap<EntityAttribute, EntityAttributeModifier> fabrication$oldArmor(Multimap<EntityAttribute, EntityAttributeModifier> map, ItemStack stack, EquipmentSlot slot, LivingEntity self) {
		final boolean scale = FabConf.isEnabled("*.old_armor_scale") && fabrication$oldArmorScalePredicate.test(self);
		final boolean old = FabConf.isEnabled("*.old_armor") && fabrication$oldArmorPredicate.test(self);
		if (!(((scale && stack.isDamageable()) || old) && stack.getItem() instanceof ArmorItem && ((ArmorItem)stack.getItem()).getSlotType() == slot)) return map;
		return map.entries().stream().map(
				entry ->
					(entry.getKey() == EntityAttributes.GENERIC_ARMOR && entry.getValue().getOperation() == EntityAttributeModifier.Operation.ADDITION ?
							new AbstractMap.SimpleEntry<>(
									entry.getKey(),
									new EntityAttributeModifier(
											entry.getValue().getId(),
											entry.getValue().getName(),
											(old ? ArmorMaterials.DIAMOND.getProtectionAmount(slot) : entry.getValue().getValue())
													* (scale ? ((stack.getMaxDamage() - stack.getDamage()) / (double) stack.getMaxDamage()) : 1),
											EntityAttributeModifier.Operation.ADDITION))
							: new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()))
				).collect(Collector.of(ArrayListMultimap::create, (m, entry) ->m.put(entry.getKey(), entry.getValue()), (m1, m2) -> {m1.putAll(m2); return m1;}));

	}
}
