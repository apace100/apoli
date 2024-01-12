package io.github.apace100.apoli.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.JsonTextFormatter;
import joptsimple.internal.Strings;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PowerCommand {

	public static Identifier POWER_SOURCE = Apoli.identifier("command");

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			literal("power").requires(scs -> scs.hasPermissionLevel(2))
				.then(literal("grant")
					.then(argument("targets", PowerHolderArgumentType.holders())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(context -> grantPower(context, false))
                            .then(argument("source", IdentifierArgumentType.identifier())
                                .executes(context -> grantPower(context, true)))))
				)
				.then(literal("revoke")
					.then(argument("targets", PowerHolderArgumentType.holders())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(context -> revokePower(context, false))
							.then(argument("source", IdentifierArgumentType.identifier())
								.executes(context -> revokePower(context, true)))))
				)
				.then(literal("revokeall")
					.then(argument("targets", PowerHolderArgumentType.holders())
						.then(argument("source", IdentifierArgumentType.identifier())
							.executes(PowerCommand::revokeAllPowers)))
				)
				.then(literal("list")
					.then(argument("target", PowerHolderArgumentType.holder())
						.executes(context -> listPowers(context, false))
						.then(argument("subpowers", BoolArgumentType.bool())
							.executes(context -> listPowers(context, true))))
				)
				.then(literal("has")
					.then(argument("targets", PowerHolderArgumentType.holders())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(PowerCommand::hasPower)))
				)
				.then(literal("sources")
					.then(argument("target", PowerHolderArgumentType.holder())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(PowerCommand::getSourcesFromPower)))
				)
				.then(literal("remove")
					.then(argument("targets", PowerHolderArgumentType.holders())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(PowerCommand::removePower)))
				)
				.then(literal("clear")
					.executes(context -> clearAllPowers(context, true))
					.then(argument("targets", PowerHolderArgumentType.holders())
						.executes(context -> clearAllPowers(context, false)))
				)
				.then(literal("dump")
					.then(argument("power", PowerTypeArgumentType.power())
						.executes(context -> dumpPowerJson(context, false))
						.then(argument("indent", IntegerArgumentType.integer(0))
							.executes(context -> dumpPowerJson(context, true))))
				)
		);
	}

	private static int grantPower(CommandContext<ServerCommandSource> context, boolean isSourceSpecified) throws CommandSyntaxException {

		ServerCommandSource source = context.getSource();

		List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
		List<LivingEntity> processedTargets = new LinkedList<>();

		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
		Identifier powerSource = isSourceSpecified ? IdentifierArgumentType.getIdentifier(context, "source") : Apoli.identifier("command");

		for (LivingEntity target : targets) {

			PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
			if (!component.addPower(powerType, powerSource)) {
				continue;
			}

			component.sync();
			processedTargets.add(target);

		}

		Text powerTypeName = powerType.getName();
		Text targetName = targets.get(0).getName();

		int targetsSize = targets.size();
		int processedTargetsSize = processedTargets.size();

		if (processedTargetsSize == 0) {

			if (targetsSize == 1) {
				source.sendError(Text.translatable("commands.apoli.grant.fail.single", targetName, powerTypeName, powerSource.toString()));
			} else {
				source.sendError(Text.translatable("commands.apoli.grant.fail.multiple", targetsSize, powerTypeName, powerSource.toString()));
			}

			return processedTargetsSize;

		}

		Text processedTargetName = processedTargets.get(0).getName();
		if (isSourceSpecified) {
			if (processedTargetsSize == 1) {
				source.sendFeedback(() -> Text.translatable("commands.apoli.grant_from_source.success.single", processedTargetName, powerTypeName, powerSource.toString()), true);
			} else {
				source.sendFeedback(() -> Text.translatable("commands.apoli.grant_from_source.success.multiple", processedTargetsSize, powerTypeName, powerSource.toString()), true);
			}
		} else {
			if (processedTargetsSize == 1) {
				source.sendFeedback(() -> Text.translatable("commands.apoli.grant.success.single", processedTargetName, powerTypeName), true);
			} else {
				source.sendFeedback(() -> Text.translatable("commands.apoli.grant.success.multiple", processedTargetsSize, powerTypeName), true);
			}
		}

		return processedTargetsSize;

	}

	private static int revokePower(CommandContext<ServerCommandSource> context, boolean isSourceSpecified) throws CommandSyntaxException {

		ServerCommandSource source = context.getSource();

		List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
		List<LivingEntity> processedTargets = new LinkedList<>();

		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
		Identifier powerSource = isSourceSpecified ? IdentifierArgumentType.getIdentifier(context, "source") : POWER_SOURCE;

		for (LivingEntity target : targets) {

			PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
			if (!component.hasPower(powerType, powerSource)) {
				continue;
			}

			component.removePower(powerType, powerSource);
			component.sync();

			processedTargets.add(target);

		}

		Text powerTypeName = powerType.getName();
		Text targetName = targets.get(0).getName();

		int targetsSize = targets.size();
		int processedTargetsSize = processedTargets.size();

		if (processedTargetsSize == 0) {

			if (targetsSize == 1) {
				source.sendError(Text.translatable("commands.apoli.revoke.fail.single", targetName, powerTypeName, powerSource.toString()));
			} else {
				source.sendError(Text.translatable("commands.apoli.revoke.fail.multiple", powerTypeName, powerSource.toString()));
			}

			return processedTargetsSize;

		}

		Text processedTargetName = processedTargets.get(0).getName();
		if (isSourceSpecified) {
			if (processedTargetsSize == 1) {
				source.sendFeedback(() -> Text.translatable("commands.apoli.revoke_from_source.success.single", processedTargetName, powerTypeName, powerSource.toString()), true);
			} else {
				source.sendFeedback(() -> Text.translatable("commands.apoli.revoke_from_source.success.multiple", processedTargetsSize, powerTypeName, powerSource.toString()), true);
			}
		} else {
			if (processedTargetsSize == 1) {
				source.sendFeedback(() -> Text.translatable("commands.apoli.revoke.success.single", processedTargetName, powerTypeName), true);
			} else {
				source.sendFeedback(() -> Text.translatable("commands.apoli.revoke.success.multiple", processedTargetsSize, powerTypeName), true);
			}
		}

		return processedTargetsSize;

	}

	private static int revokeAllPowers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

		ServerCommandSource source = context.getSource();

		List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
		List<LivingEntity> processedTargets = new LinkedList<>();

		Identifier powerSource = IdentifierArgumentType.getIdentifier(context, "source");
		int revokedPowers = 0;

		for (LivingEntity target : targets) {

			PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
			int revokedPowersFromSource = component.removeAllPowersFromSource(powerSource);

			if (revokedPowersFromSource == 0) {
				continue;
			}

			component.sync();
			processedTargets.add(target);

			revokedPowers += revokedPowersFromSource;

		}

		Text targetName = targets.get(0).getName();

		int targetsSize = targets.size();
		int processedTargetsSize = processedTargets.size();

		if (processedTargetsSize == 0) {
			if (targetsSize == 1) {
				source.sendError(Text.translatable("commands.apoli.revoke_all.fail.single", targetName, powerSource.toString()));
			} else {
				source.sendError(Text.stringifiedTranslatable("commands.apoli.revoke_all.fail.multiple", powerSource));
			}
		} else {

			Text processedTargetName = processedTargets.get(0).getName();
			int finalRevokedPowers = revokedPowers;

			if (processedTargetsSize == 1) {
				source.sendFeedback(() -> Text.translatable("commands.apoli.revoke_all.success.single", processedTargetName, finalRevokedPowers, powerSource.toString()), true);
			} else {
				source.sendFeedback(() -> Text.translatable("commands.apoli.revoke_all.success.multiple", processedTargetsSize, finalRevokedPowers, powerSource.toString()), true);
			}

		}

		return processedTargetsSize;

	}

	private static int listPowers(CommandContext<ServerCommandSource> context, boolean includeSubpowers) throws CommandSyntaxException {

		ServerCommandSource source = context.getSource();
		LivingEntity target = PowerHolderArgumentType.getHolder(context, "target");

		List<Text> powersTooltip = new LinkedList<>();
		int powers = 0;

		PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
		for (PowerType<?> powerType : component.getPowerTypes(includeSubpowers)) {

			List<Text> sourcesTooltip = new LinkedList<>();
			component.getSources(powerType).forEach(id -> sourcesTooltip.add(Text.of(id.toString())));

			HoverEvent sourceHoverEvent = new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				Text.translatable("commands.apoli.list.sources", Texts.join(sourcesTooltip, Text.of(",")))
			);

			Text powerTooltip = Text.literal(powerType.getIdentifier().toString())
				.setStyle(Style.EMPTY.withHoverEvent(sourceHoverEvent));

			powersTooltip.add(powerTooltip);
			powers++;

		}

		if (powers == 0) {
			source.sendError(Text.translatable("commands.apoli.list.fail", target.getName()));
		} else {
			int finalPowers = powers;
			source.sendFeedback(() -> Text.translatable("commands.apoli.list.pass", target.getName(), finalPowers, Texts.join(powersTooltip, Text.of(", "))), true);
		}

		return powers;

	}

	private static int hasPower(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

		ServerCommandSource source = context.getSource();

		List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
		List<LivingEntity> processedTargets = new LinkedList<>();

		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");

		for (LivingEntity target : targets) {
			PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
			if (component.hasPower(powerType)) {
				processedTargets.add(target);
			}
		}

		int targetsSize = targets.size();
		int processedTargetsSize = processedTargets.size();

		if (processedTargetsSize == 0) {
			if (targetsSize == 1) {
				source.sendError(Text.translatable("commands.execute.conditional.fail"));
			} else {
				source.sendError(Text.translatable("commands.execute.conditional.fail_count", targetsSize));
			}
		} else {
			if (processedTargetsSize == 1) {
				source.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), true);
			} else {
				source.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass_count", processedTargetsSize), true);
			}
		}

		return processedTargets.size();

	}

	private static int getSourcesFromPower(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

		ServerCommandSource source = context.getSource();

		LivingEntity target = PowerHolderArgumentType.getHolder(context, "target");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");

		PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
		StringBuilder powerSources = new StringBuilder();
		int powers = 0;

		String separator = "";
		for (Identifier powerSource : component.getSources(powerType)) {

			powerSources.append(separator).append(powerSource.toString());
			powers++;

			separator = ", ";

		}

		if (powers == 0) {
			source.sendError(Text.translatable("commands.apoli.sources.fail", target.getName(), powerType.getName()));
		} else {
			int finalPowers = powers;
			source.sendFeedback(() -> Text.translatable("commands.apoli.sources.pass", target.getName(), finalPowers, powerType.getName(), powerSources.toString()), true);
		}

		return powers;

	}

	private static int removePower(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

		ServerCommandSource source = context.getSource();

		List<LivingEntity> targets = PowerHolderArgumentType.getHolders(context, "targets");
		List<LivingEntity> processedTargets = new LinkedList<>();

		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");

		for (LivingEntity target : targets) {

			PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
			List<Identifier> powerSources = component.getSources(powerType);
			if (powerSources.isEmpty()) {
				continue;
			}

			for (Identifier powerSource : component.getSources(powerType)) {
				component.removePower(powerType, powerSource);
			}

			component.sync();
			processedTargets.add(target);

		}

		Text targetName = targets.get(0).getName();
		Text powerTypeName = powerType.getName();

		int targetsSize = targets.size();
		int processedTargetsSize = processedTargets.size();

		if (processedTargetsSize == 0) {
			if (targetsSize == 1) {
				source.sendError(Text.translatable("commands.apoli.remove.fail.single", targetName, powerTypeName));
			} else {
				source.sendError(Text.translatable("commands.apoli.remove.fail.multiple", powerTypeName));
			}
		} else {
			Text processedTargetName = processedTargets.get(0).getName();
			if (processedTargetsSize == 1) {
				source.sendFeedback(() -> Text.translatable("commands.apoli.remove.success.single", processedTargetName, powerTypeName), true);
			} else {
				source.sendFeedback(() -> Text.translatable("commands.apoli.remove.success.multiple", processedTargetsSize, powerTypeName), true);
			}
		}

		return processedTargetsSize;

	}

	private static int clearAllPowers(CommandContext<ServerCommandSource> context, boolean onlyTargetSelf) throws CommandSyntaxException {

		ServerCommandSource source = context.getSource();

		List<LivingEntity> targets = new LinkedList<>();
		List<LivingEntity> processedTargets = new LinkedList<>();

		if (!onlyTargetSelf) {
			targets.addAll(PowerHolderArgumentType.getHolders(context, "targets"));
		} else {

			Entity self = source.getEntityOrThrow();
			if (!(self instanceof LivingEntity livingSelf)) {
				throw PowerHolderArgumentType.HOLDER_NOT_FOUND.create(self.getName());
			}

			targets.add(livingSelf);

		}

		int clearedPowers = 0;

		for (LivingEntity target : targets) {

			PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
			Set<PowerType<?>> powerTypes = component.getPowerTypes(false);
			if (powerTypes.isEmpty()) {
				continue;
			}

			for (PowerType<?> powerType : powerTypes) {
				List<Identifier> powerSources = component.getSources(powerType);
				powerSources.forEach(component::removeAllPowersFromSource);
			}

			component.sync();
			clearedPowers += powerTypes.size();
			processedTargets.add(target);

		}

		Text targetName = targets.get(0).getName();

		int targetsSize = targets.size();
		int processedTargetsSize = processedTargets.size();

		if (processedTargetsSize == 0) {
			if (targetsSize == 1) {
				source.sendError(Text.translatable("commands.apoli.clear.fail.single", targetName));
			} else {
				source.sendError(Text.translatable("commands.apoli.clear.fail.multiple"));
			}
		} else {

			Text processedTargetName = processedTargets.get(0).getName();
			int finalClearedPowers = clearedPowers;

			if (processedTargetsSize == 1) {
				source.sendFeedback(() -> Text.translatable("commands.apoli.clear.success.single", processedTargetName, finalClearedPowers), true);
			} else {
				source.sendFeedback(() -> Text.translatable("commands.apoli.clear.success.multiple", processedTargetsSize, finalClearedPowers), true);
			}

		}

		return clearedPowers;

	}

	private static int dumpPowerJson(CommandContext<ServerCommandSource> context, boolean indentSpecified) throws CommandSyntaxException {

		ServerCommandSource source = context.getSource();
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");

		String indent = Strings.repeat(' ', indentSpecified ? IntegerArgumentType.getInteger(context, "indent") : 4);
		source.sendFeedback(() -> new JsonTextFormatter(indent).apply(powerType.toJson()), false);

		return 1;

	}

}
