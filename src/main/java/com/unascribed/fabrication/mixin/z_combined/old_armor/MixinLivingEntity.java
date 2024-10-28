package com.unascribed.fabrication.mixin.z_combined.old_armor;

import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(anyConfigAvailable={"*.old_armor_scale", "*.old_armor"})
@FailOn(invertedSpecialConditions={SpecialEligibility.FORGE, SpecialEligibility.NOT_FORGE})
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private static final Predicate<LivingEntity> fabrication$oldArmorScalePredicate = ConfigPredicates.getFinalPredicate("*.old_armor_scale");
	private static final Predicate<LivingEntity> fabrication$oldArmorPredicate = ConfigPredicates.getFinalPredicate("*.old_armor");
	//TODO
//	@ModifyReturn(target="Lnet/minecraft/item/ItemStack;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;",
//			method="getEquipmentChanges()Ljava/util/Map;")
//	private static Multimap<EntityAttribute, EntityAttributeModifier> fabrication$oldArmor(Multimap<EntityAttribute, EntityAttributeModifier> map, ItemStack stack, EquipmentSlot slot, LivingEntity self) {
//		final boolean scale = FabConf.isEnabled("*.old_armor_scale") && fabrication$oldArmorScalePredicate.test(self);
//		final boolean old = FabConf.isEnabled("*.old_armor") && fabrication$oldArmorPredicate.test(self);
//		if (!(((scale && stack.isDamageable()) || old) && stack.getItem() instanceof ArmorItem && ((ArmorItem)stack.getItem()).getSlotType() == slot)) return map;
//		return map.entries().stream().map(
//				entry ->
//					(entry.getKey() == EntityAttributes.GENERIC_ARMOR && entry.getValue().operation() == EntityAttributeModifier.Operation.ADD_VALUE ?
//							new AbstractMap.SimpleEntry<>(
//									entry.getKey(),
//									new EntityAttributeModifier(
//											entry.getValue().id(),
//											(old ? ArmorMaterials.DIAMOND.value().getProtection(((ArmorItem)stack.getItem()).getType()) : entry.getValue().value())
//													* (scale ? ((stack.getMaxDamage() - stack.getDamage()) / (double) stack.getMaxDamage()) : 1),
//											EntityAttributeModifier.Operation.ADD_VALUE))
//							: new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()))
//				).collect(Collector.of(ArrayListMultimap::create, (m, entry) ->m.put(entry.getKey(), entry.getValue()), (m1, m2) -> {m1.putAll(m2); return m1;}));
//
//	}
}
