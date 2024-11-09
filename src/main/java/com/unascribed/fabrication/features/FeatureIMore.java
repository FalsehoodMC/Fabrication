package com.unascribed.fabrication.features;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.unascribed.fabrication.Agnos;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabRefl;
import com.unascribed.fabrication.support.EligibleIf;
import com.unascribed.fabrication.support.Feature;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EnchantmentArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;

@EligibleIf(configAvailable="*.i_and_more")
public class FeatureIMore implements Feature {

	private boolean applied = false;
	private boolean registered = false;

	@Override
	public void apply() {
		applied = true;
		if (!registered) {
			registered = true;
			Agnos.runForCommandRegistration((dispatcher, dedi) -> {
				Predicate<ServerCommandSource> requirement = s -> s.hasPermissionLevel(2) && FabConf.isEnabled("*.i_and_more") && applied;
				// I tried redirect(). It doesn't work.
				String[] itemCommandNames = { "item", "i" };
				for (String name : itemCommandNames) {
					dispatcher.register(CommandManager.literal(name)
							.requires(requirement)
							.then(CommandManager.argument("item", ItemStackArgumentType.itemStack())
									.then(CommandManager.argument("count", IntegerArgumentType.integer(1))
											.executes(this::item))
									.executes(this::item)));
				}
				dispatcher.register(CommandManager.literal("more")
						.requires(requirement)
						.then(CommandManager.argument("count", IntegerArgumentType.integer(1))
								.executes((c) -> more(c, EquipmentSlot.MAINHAND)))
						.then(CommandManager.literal("main")
								.then(CommandManager.argument("count", IntegerArgumentType.integer(1))
										.executes((c) -> more(c, EquipmentSlot.MAINHAND))))
						.then(CommandManager.literal("off")
								.then(CommandManager.argument("count", IntegerArgumentType.integer(1))
										.executes((c) -> more(c, EquipmentSlot.OFFHAND))))
						.then(CommandManager.literal("both")
								.then(CommandManager.argument("count", IntegerArgumentType.integer(1))
										.executes((c) -> { more(c, EquipmentSlot.OFFHAND); return more(c, EquipmentSlot.MAINHAND); })))
						.executes((c) -> more(c, EquipmentSlot.MAINHAND)));
				dispatcher.register(CommandManager.literal("fenchant")
						.requires(requirement)
						.then(CommandManager.argument("targets", EntityArgumentType.entities())
								.then(CommandManager.argument("enchantment", EnchantmentArgumentType.enchantment())
										.executes((ctx) -> fenchant(ctx.getSource(), EntityArgumentType.getEntities(ctx, "targets"), EnchantmentArgumentType.getEnchantment(ctx, "enchantment"), 1))
										.then(CommandManager.argument("level", IntegerArgumentType.integer(0))
												.executes((ctx) -> fenchant(ctx.getSource(), EntityArgumentType.getEntities(ctx, "targets"), EnchantmentArgumentType.getEnchantment(ctx, "enchantment"), IntegerArgumentType.getInteger(ctx, "level")))))));
			});
		}
	}

	public int fenchant(ServerCommandSource source, Collection<? extends Entity> targets, Enchantment enchantment, int level) throws CommandSyntaxException {
		int amt = 0;
		for (Entity e : targets) {
			if (e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity)e;
				ItemStack stack = le.getMainHandStack();
				if (!stack.isEmpty()) {
					NbtCompound tag = stack.getOrCreateNbt();
					if (!tag.contains("Enchantments", 9)) {
						tag.put("Enchantments", new NbtList());
					}

					NbtList ench = tag.getList("Enchantments", NbtType.COMPOUND);
					NbtCompound c = new NbtCompound();
					c.putString("id", String.valueOf(Registry.ENCHANTMENT.getId(enchantment)));
					c.putInt("lvl", level);
					ench.add(c);
					amt++;
				} else if (targets.size() == 1) {
					throw new CommandException(new TranslatableText("commands.enchant.failed.itemless", le.getName()));
				}
			} else if (targets.size() == 1) {
				throw new CommandException(new TranslatableText("commands.enchant.failed.entity", e.getName()));
			}
		}

		if (amt == 0) {
			throw new CommandException(new TranslatableText("commands.enchant.failed"));
		}

		if (targets.size() == 1) {
			source.sendFeedback(new TranslatableText("commands.enchant.success.single", enchantment.getName(level), targets.iterator().next().getDisplayName()), true);
		} else {
			source.sendFeedback(new TranslatableText("commands.enchant.success.multiple", enchantment.getName(level), targets.size()), true);
		}

		return amt;
	}

	public int more(CommandContext<ServerCommandSource> ctx, EquipmentSlot slot) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayer();
		ItemStack held = player.getEquippedStack(slot);
		if (held.isEmpty()) {
			throw new CommandException(new LiteralText("Cannot duplicate an empty stack"));
		}
		int count;
		try {
			count = ctx.getArgument("count", Integer.class);
			if (count > held.getMaxCount()) {
				throw new CommandException(new TranslatableText("arguments.item.overstacked", held.getName(), held.getMaxCount()));
			}
		} catch (IllegalArgumentException e) {
			count = held.getMaxCount();
		}
		if (held.getCount() == count) {
			throw new CommandException(new LiteralText("Your stack is already that large"));
		} else if (held.getCount() > count) {
			throw new CommandException(new LiteralText("Your stack is already bigger than that"));
		}
		int amt = count-held.getCount();
		ctx.getSource().sendFeedback(new TranslatableText("commands.give.success.single", amt, held.toHoverableText(), player.getDisplayName()), true);
		held.setCount(count);
		player.equipStack(slot, held);
		return 1;
	}

	public int item(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		int count;
		try {
			count = ctx.getArgument("count", Integer.class);
		} catch (IllegalArgumentException e) {
			count = 1;
		}
		return FabRefl.GiveCommand_execute(ctx.getSource(), ctx.getArgument("item", ItemStackArgument.class), Collections.singleton(ctx.getSource().getPlayer()), count);
	}

	@Override
	public boolean undo() {
		applied = false;
		return true;
	}

	@Override
	public String getConfigKey() {
		return "*.i_and_more";
	}

}
