package com.unascribed.fabrication.features;

import java.util.List;
import java.util.Set;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.unascribed.ears.api.EarsStateType;
import com.unascribed.ears.api.OverrideResult;
import com.unascribed.ears.api.registry.EarsStateOverriderRegistry;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.interfaces.GetSuppressedSlots;
import com.unascribed.fabrication.interfaces.SetFabricationConfigAware;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Env;
import com.unascribed.fabrication.support.Feature;

import com.google.common.collect.Lists;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@EligibleIf(configAvailable="*.hide_armor")
public class FeatureHideArmor implements Feature {

	private static final EquipmentSlot[] ALL_ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

	private boolean applied = false;
	private boolean registered = false;

	@Override
	public void apply() {
		applied = true;
		if (!registered) {
			registered = true;
			Agnos.runForCommandRegistration((dispatcher, registryAccess, dedi) -> {
				dispatcher.register(buildCommand("hidearmor", true));
				dispatcher.register(buildCommand("showarmor", false));
			});
			if (Agnos.getCurrentEnv() == Env.CLIENT && Agnos.isModLoaded("ears")) {
				try {
					EarsCompat.init();
				} catch (Throwable t) {}
			}
		}
	}

	private static final class EarsCompat {
		public static void init() {
			EarsStateOverriderRegistry.register("fabrication", (state, peer) -> {
				if (!FabConf.isEnabled("*.hide_armor")) return OverrideResult.DEFAULT;
				if (!(peer instanceof GetSuppressedSlots)) return OverrideResult.DEFAULT;
				GetSuppressedSlots gsg = (GetSuppressedSlots)peer;
				if (state == EarsStateType.WEARING_HELMET && gsg.fabrication$getSuppressedSlots().contains(EquipmentSlot.HEAD)) {
					return OverrideResult.FALSE;
				}
				if (state == EarsStateType.WEARING_CHESTPLATE && gsg.fabrication$getSuppressedSlots().contains(EquipmentSlot.CHEST)) {
					return OverrideResult.FALSE;
				}
				if (state == EarsStateType.WEARING_LEGGINGS && gsg.fabrication$getSuppressedSlots().contains(EquipmentSlot.LEGS)) {
					return OverrideResult.FALSE;
				}
				if (state == EarsStateType.WEARING_BOOTS && gsg.fabrication$getSuppressedSlots().contains(EquipmentSlot.FEET)) {
					return OverrideResult.FALSE;
				}
				return OverrideResult.DEFAULT;
			});
		}
	}

	private LiteralArgumentBuilder<ServerCommandSource> buildCommand(String cmd, boolean hidden) {
		return CommandManager.literal(cmd)
				.requires(scs -> FabConf.isEnabled("*.hide_armor") && applied)
				.then(CommandManager.literal("all").executes((c) -> setArmorHidden(c, hidden, ALL_ARMOR)))
				.then(CommandManager.literal("head").executes((c) -> setArmorHidden(c, hidden, EquipmentSlot.HEAD)))
				.then(CommandManager.literal("chest").executes((c) -> setArmorHidden(c, hidden, EquipmentSlot.CHEST)))
				.then(CommandManager.literal("legs").executes((c) -> setArmorHidden(c, hidden, EquipmentSlot.LEGS)))
				.then(CommandManager.literal("feet").executes((c) -> setArmorHidden(c, hidden, EquipmentSlot.FEET)))
				.then(CommandManager.literal("!head").executes((c) -> setArmorHidden(c, hidden, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)))
				.then(CommandManager.literal("!chest").executes((c) -> setArmorHidden(c, hidden, EquipmentSlot.HEAD, EquipmentSlot.LEGS, EquipmentSlot.FEET)))
				.then(CommandManager.literal("!legs").executes((c) -> setArmorHidden(c, hidden, EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.FEET)))
				.then(CommandManager.literal("!feet").executes((c) -> setArmorHidden(c, hidden, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.HEAD)))
				.executes((c) -> setArmorHidden(c, hidden, ALL_ARMOR));
	}

	private int setArmorHidden(CommandContext<ServerCommandSource> c, boolean hidden, EquipmentSlot... slots) throws CommandSyntaxException {
		ServerPlayerEntity ent = c.getSource().getPlayerOrThrow();
		if (ent instanceof GetSuppressedSlots) {
			Set<EquipmentSlot> suppressed = ((GetSuppressedSlots)ent).fabrication$getSuppressedSlots();
			int amt = 0;
			for (EquipmentSlot es : slots) {
				if (hidden) {
					if (suppressed.add(es)) {
						amt++;
					}
				} else {
					if (suppressed.remove(es)) {
						amt++;
					}
				}
			}
			List<Pair<EquipmentSlot, ItemStack>> li = Lists.newArrayList();
			for (EquipmentSlot es : EquipmentSlot.values()) {
				li.add(Pair.of(es, suppressed.contains(es) ? ItemStack.EMPTY : ent.getEquippedStack(es)));
			}
			((ServerWorld)ent.world).getChunkManager().sendToOtherNearbyPlayers(ent, new EntityEquipmentUpdateS2CPacket(ent.getId(), li));
			sendSuppressedSlotsForSelf(ent);
			String verb = hidden ? "hidden" : "shown";
			if (amt == 4) {
				c.getSource().sendFeedback(Text.literal("All armor slots "+verb), false);
			} else if (amt > 1) {
				c.getSource().sendFeedback(Text.literal(amt+" armor slots "+verb), false);
			} else if (amt > 0) {
				c.getSource().sendFeedback(Text.literal("1 armor slot "+verb), false);
			} else {
				c.getSource().sendFeedback(Text.literal("All specified slots are already "+verb), false);
			}
		} else {
			c.getSource().sendFeedback(Text.literal("Patch error!"), false);
		}
		return 1;
	}

	@Override
	public boolean undo() {
		applied = false;
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.hide_armor";
	}

	public static List<Pair<EquipmentSlot, ItemStack>> muddle(Entity entity, List<Pair<EquipmentSlot, ItemStack>> equipmentList) {
		if (FabConf.isEnabled("*.hide_armor")) {
			if (entity instanceof GetSuppressedSlots) {
				Set<EquipmentSlot> slots = ((GetSuppressedSlots) entity).fabrication$getSuppressedSlots();
				return Lists.transform(equipmentList, (pair) -> slots.contains(pair.getFirst()) ? Pair.of(pair.getFirst(), ItemStack.EMPTY) : pair);
			}
		}
		return equipmentList;
	}

	public static void sendSuppressedSlotsForSelf(ServerPlayerEntity ent) {
		// we need client support for self-hiding of armor to prevent the inventory from
		// glitching out, as we're lying to the client about what armor is equipped
		if (ent instanceof SetFabricationConfigAware && ((SetFabricationConfigAware)ent).fabrication$isConfigAware()) {
			PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
			int bits = 0;
			for (EquipmentSlot es : ((GetSuppressedSlots)ent).fabrication$getSuppressedSlots()) {
				bits |= 1 << es.getEntitySlotId();
			}
			data.writeVarInt(bits);
			CustomPayloadS2CPacket pkt = new CustomPayloadS2CPacket(new Identifier("fabrication", "hide_armor"), data);
			ent.networkHandler.sendPacket(pkt);
		}
	}

}
