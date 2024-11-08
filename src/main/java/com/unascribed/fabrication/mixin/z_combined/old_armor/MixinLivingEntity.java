package com.unascribed.fabrication.mixin.z_combined.old_armor;

import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.support.ConfigPredicates;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.FailOn;
import com.unascribed.fabrication.support.SpecialEligibility;
import com.unascribed.fabrication.support.injection.Hijack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

@Mixin(LivingEntity.class)
@EligibleIf(anyConfigAvailable={"*.old_armor_scale", "*.old_armor"})
public abstract class MixinLivingEntity extends Entity {

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	private static final Predicate<LivingEntity> fabrication$oldArmorScalePredicate = ConfigPredicates.getFinalPredicate("*.old_armor_scale");
	private static final Predicate<LivingEntity> fabrication$oldArmorPredicate = ConfigPredicates.getFinalPredicate("*.old_armor");

	@Hijack(target="Lnet/minecraft/item/ItemStack;applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
			method="getEquipmentChanges()Ljava/util/Map;")
	private static boolean fabrication$oldArmor(ItemStack stack, EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> consumer, LivingEntity self) {
		final boolean scale = FabConf.isEnabled("*.old_armor_scale") && fabrication$oldArmorScalePredicate.test(self);
		final boolean old = FabConf.isEnabled("*.old_armor") && fabrication$oldArmorPredicate.test(self);
		if (!(((scale && stack.isDamageable()) || old) && stack.getItem() instanceof ArmorItem && ((ArmorItem)stack.getItem()).getSlotType() == slot)) return false;
		stack.applyAttributeModifiers(slot, ((value, entry) -> {
			if (value == EntityAttributes.GENERIC_ARMOR && entry.operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
				entry = new EntityAttributeModifier(
					entry.id(),
					(old ? ArmorMaterials.DIAMOND.value().getProtection(((ArmorItem)stack.getItem()).getType()) : entry.value())
						* (scale ? ((stack.getMaxDamage() - stack.getDamage()) / (double) stack.getMaxDamage()) : 1),
					EntityAttributeModifier.Operation.ADD_VALUE);
			}
			consumer.accept(value, entry);
		}));
		return true;
	}
}
