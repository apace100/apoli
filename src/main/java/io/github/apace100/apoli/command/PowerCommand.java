package io.github.apace100.apoli.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PowerCommand {

	private enum OptionType {
		SPECIFIED,
		NOT_SPECIFIED
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			literal("power").requires(scs -> scs.hasPermissionLevel(2))
				.then(literal("grant")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(context -> grantPower(context, OptionType.NOT_SPECIFIED)))
						.then(argument("source", IdentifierArgumentType.identifier())
							.executes(context -> grantPower(context, OptionType.SPECIFIED))))
				)
				.then(literal("revoke")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(context -> revokePower(context, OptionType.NOT_SPECIFIED))
							.then(argument("source", IdentifierArgumentType.identifier())
								.executes(context -> revokePower(context, OptionType.SPECIFIED)))))
				)
				.then(literal("revokeall")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("source", IdentifierArgumentType.identifier())
							.executes(PowerCommand::revokeAllPowers)))
				)
				.then(literal("list")
					.then(argument("target", EntityArgumentType.entity())
						.executes(context -> listPowers(context, OptionType.NOT_SPECIFIED))
						.then(argument("subpowers", BoolArgumentType.bool())
							.executes(context -> listPowers(context, OptionType.SPECIFIED))))
				)
				.then(literal("has")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(PowerCommand::hasPower)))
				)
				.then(literal("sources")
					.then(argument("target", EntityArgumentType.entity())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(PowerCommand::getSourcesFromPower)))
				)
				.then(literal("remove")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes(PowerCommand::removePower)))
				)
				.then(literal("clear")
					.then(argument("targets", EntityArgumentType.entities())
						.executes(PowerCommand::clearAllPowers))
				)
		);
	}

	private static int grantPower(CommandContext<ServerCommandSource> context, OptionType optionType) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = context.getSource();
		Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
		Identifier powerSource = switch (optionType) {
			case SPECIFIED -> IdentifierArgumentType.getIdentifier(context, "source");
			case NOT_SPECIFIED -> Apoli.identifier("command");
		};

		List<Entity> nonLivingTargets = new ArrayList<>();
		List<LivingEntity> livingTargets = new ArrayList<>();
		List<LivingEntity> processedLivingTargets = new ArrayList<>();

		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) {
				nonLivingTargets.add(target);
				continue;
			}

			livingTargets.add(livingTarget);
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);

			if (!powerHolderComponent.addPower(powerType, powerSource)) continue;

			powerHolderComponent.sync();
			processedLivingTargets.add(livingTarget);

		}

		if (!processedLivingTargets.isEmpty()) {
			if (optionType == OptionType.NOT_SPECIFIED) {
				if (processedLivingTargets.size() == 1) serverCommandSource.sendFeedback(Text.translatable("commands.apoli.grant.success.single", processedLivingTargets.get(0).getDisplayName(), powerType.getName()), true);
				else serverCommandSource.sendFeedback(Text.translatable("commands.apoli.grant.success.multiple", processedLivingTargets.size(), powerType.getName()), true);
			}
			else {
				if (processedLivingTargets.size() == 1) serverCommandSource.sendFeedback(Text.translatable("commands.apoli.grant_from_source.success.single", processedLivingTargets.get(0).getDisplayName(), powerType.getName(), powerSource), true);
				else serverCommandSource.sendFeedback(Text.translatable("commands.apoli.grant_from_source.success.multiple", processedLivingTargets.size(), powerType.getName(), powerSource), true);
			}
		}

		else if (!livingTargets.isEmpty()) {
			if (livingTargets.size() == 1) serverCommandSource.sendError(Text.translatable("commands.apoli.grant.fail.single", livingTargets.get(0).getDisplayName(), powerType.getName(), powerSource));
			else serverCommandSource.sendError(Text.translatable("commands.apoli.grant.fail.multiple", livingTargets.size(), powerType.getName(), powerSource));
		}

		else if (!nonLivingTargets.isEmpty()) {
			if (nonLivingTargets.size() == 1) serverCommandSource.sendError(Text.translatable("commands.apoli.non_living_entity", nonLivingTargets.get(0).getDisplayName()));
			else serverCommandSource.sendError(Text.translatable("commands.apoli.non_living_entities", nonLivingTargets.size()));
		}

		return processedLivingTargets.size();

	}

	private static int revokePower(CommandContext<ServerCommandSource> context, OptionType optionType) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = context.getSource();
		Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
		Identifier powerSource = switch (optionType) {
			case SPECIFIED -> IdentifierArgumentType.getIdentifier(context, "source");
			case NOT_SPECIFIED -> Apoli.identifier("command");
		};

		List<Entity> nonLivingTargets = new ArrayList<>();
		List<LivingEntity> livingTargets = new ArrayList<>();
		List<LivingEntity> processedLivingTargets = new ArrayList<>();

		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) {
				nonLivingTargets.add(target);
				continue;
			}

			livingTargets.add(livingTarget);
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);

			if (!(powerHolderComponent.hasPower(powerType, powerSource))) continue;

			powerHolderComponent.removePower(powerType, powerSource);
			powerHolderComponent.sync();
			processedLivingTargets.add(livingTarget);

		}

		if (!processedLivingTargets.isEmpty()) {
			if (optionType == OptionType.NOT_SPECIFIED) {
				if (processedLivingTargets.size() == 1) serverCommandSource.sendFeedback(Text.translatable("commands.apoli.revoke.success.single", processedLivingTargets.get(0).getDisplayName(), powerType.getName()), true);
				else serverCommandSource.sendFeedback(Text.translatable("commands.apoli.revoke.success.multiple", processedLivingTargets.size(), powerType.getName()), true);
			}
			else {
				if (processedLivingTargets.size() == 1) serverCommandSource.sendFeedback(Text.translatable("commands.apoli.revoke_from_source.success.single", processedLivingTargets.get(0).getDisplayName(), powerType.getName(), powerSource), true);
				else serverCommandSource.sendFeedback(Text.translatable("commands.apoli.revoke_from_source.success.multiple", processedLivingTargets.size(), powerType.getName(), powerSource), true);
			}
		}

		else if (!livingTargets.isEmpty()) {
			if (livingTargets.size() == 1) serverCommandSource.sendError(Text.translatable("commands.apoli.revoke.fail.single", livingTargets.get(0).getDisplayName(), powerType.getName(), powerSource));
			else serverCommandSource.sendError(Text.translatable("commands.apoli.revoke.fail.multiple", powerType.getName(), powerSource));
		}

		else if (!nonLivingTargets.isEmpty()) {
			if (nonLivingTargets.size() == 1) serverCommandSource.sendError(Text.translatable("commands.apoli.non_living_entity", nonLivingTargets.get(0).getDisplayName()));
			else serverCommandSource.sendError(Text.translatable("commands.apoli.non_living_entities", nonLivingTargets.size()));
		}

		return processedLivingTargets.size();

	}

	private static int revokeAllPowers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = context.getSource();
		Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
		Identifier powerSource = IdentifierArgumentType.getIdentifier(context, "source");

		int revokedPowers = 0;
		List<Entity> nonLivingTargets = new ArrayList<>();
		List<LivingEntity> livingTargets = new ArrayList<>();
		List<LivingEntity> processedLivingTargets = new ArrayList<>();

		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) {
				nonLivingTargets.add(target);
				continue;
			}

			livingTargets.add(livingTarget);
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(target);
			int i = powerHolderComponent.removeAllPowersFromSource(powerSource);
			if (i <= 0) continue;

			powerHolderComponent.sync();
			revokedPowers += i;
			processedLivingTargets.add(livingTarget);

		}

		if (!processedLivingTargets.isEmpty()) {
			if (processedLivingTargets.size() == 1) serverCommandSource.sendFeedback(Text.translatable("commands.apoli.revoke_all.success.single", processedLivingTargets.get(0).getDisplayName(), revokedPowers, powerSource), true);
			else serverCommandSource.sendFeedback(Text.translatable("commands.apoli.revoke_all.success.multiple", processedLivingTargets.size(), revokedPowers, powerSource), true);
		}

		else if (!livingTargets.isEmpty()) {
			if (livingTargets.size() == 1) serverCommandSource.sendError(Text.translatable("commands.apoli.revoke_all.fail.single", livingTargets.get(0).getDisplayName(), powerSource));
			else serverCommandSource.sendError(Text.translatable("commands.apoli.revoke_all.fail.multiple", powerSource));
		}

		else if (!nonLivingTargets.isEmpty()) {
			if (nonLivingTargets.size() == 1) serverCommandSource.sendError(Text.translatable("commands.apoli.non_living_entity", nonLivingTargets.get(0).getDisplayName()));
			else serverCommandSource.sendError(Text.translatable("commands.apoli.non_living_entities", nonLivingTargets.size()));
		}

		return processedLivingTargets.size();

	}

	private static int listPowers(CommandContext<ServerCommandSource> context, OptionType optionType) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = context.getSource();
		Entity target = EntityArgumentType.getEntity(context, "target");
		StringBuilder powers = new StringBuilder();

		int powerCount = 0;

		if (!(target instanceof LivingEntity livingEntity)) {
			serverCommandSource.sendError(Text.translatable("commands.apoli.list.fail"));
			return powerCount;
		}

		PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingEntity);
		for (PowerType<?> powerType : powerHolderComponent.getPowerTypes(optionType == OptionType.SPECIFIED)) {
			if (powerCount > 0) powers.append(", ");
			powers.append(powerType.getIdentifier().toString());
			powerCount++;
		}

		serverCommandSource.sendFeedback(Text.translatable("commands.apoli.list.pass", powerCount, powers), true);
		return powerCount;

	}

	private static int hasPower(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = context.getSource();
		Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "entities");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");

		int hasPowerCount = 0;
		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) continue;

			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);
			if (!powerHolderComponent.hasPower(powerType)) continue;
			++hasPowerCount;

		}

		if (hasPowerCount == 1) serverCommandSource.sendFeedback(Text.translatable("commands.execute.conditional.pass_count", hasPowerCount), true);
		else if (hasPowerCount > 1) serverCommandSource.sendFeedback(Text.translatable("commands.execute.conditional.pass", hasPowerCount), true);
		else serverCommandSource.sendError(Text.translatable("commands.execute.conditional.fail"));

		return hasPowerCount;

	}

	private static int getSourcesFromPower(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = context.getSource();
		Entity target = EntityArgumentType.getEntity(context, "target");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
		StringBuilder powerSources = new StringBuilder();

		int powerSourceCount = 0;

		if (!(target instanceof LivingEntity livingTarget)) {
			serverCommandSource.sendError(Text.translatable("commands.apoli.sources.fail", target.getDisplayName(), powerType.getName()));
			return powerSourceCount;
		}

		PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);
		for (Identifier powerSource : powerHolderComponent.getSources(powerType)) {
			if (powerSourceCount > 0) powerSources.append(", ");
			powerSources.append(powerSource.toString());
			powerSourceCount++;
		}

		serverCommandSource.sendFeedback(Text.translatable("commands.apoli.sources.pass", livingTarget.getDisplayName(), powerSourceCount, powerSources), true);
		return powerSourceCount;

	}

	private static int removePower(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = context.getSource();
		Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
		PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");

		List<Entity> nonLivingTargets = new ArrayList<>();
		List<LivingEntity> livingTargets = new ArrayList<>();
		List<LivingEntity> processedLivingTargets = new ArrayList<>();

		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) {
				nonLivingTargets.add(target);
				continue;
			}

			livingTargets.add(livingTarget);
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);
			List<Identifier> powerSources = powerHolderComponent.getSources(powerType);
			for (Identifier powerSource : powerSources) {
				powerHolderComponent.removePower(powerType, powerSource);
			}

			if (!powerSources.isEmpty()) powerHolderComponent.sync();
			processedLivingTargets.add(livingTarget);

		}

		if (!processedLivingTargets.isEmpty()) {
			if (processedLivingTargets.size() == 1) serverCommandSource.sendFeedback(Text.translatable("commands.apoli.remove.success.single", processedLivingTargets.get(0).getDisplayName(), powerType.getName()), true);
			else serverCommandSource.sendFeedback(Text.translatable("commands.apoli.remove.success.multiple", processedLivingTargets.size(), powerType.getName()), true);
		}

		else if (!livingTargets.isEmpty()) {
			if (livingTargets.size() == 1) serverCommandSource.sendError(Text.translatable("commands.apoli.remove.fail.single", livingTargets.get(0).getDisplayName(), powerType));
			else serverCommandSource.sendError(Text.translatable("commands.apoli.remove.fail.multiple", powerType.getName()));
		}

		else if (!nonLivingTargets.isEmpty()) {
			if (nonLivingTargets.size() == 1) serverCommandSource.sendError(Text.translatable("commands.apoli.non_living_entity", nonLivingTargets.get(0).getDisplayName()));
			else serverCommandSource.sendError(Text.translatable("commands.apoli.non_living_entities", nonLivingTargets.size()));
		}

		return processedLivingTargets.size();

	}

	private static int clearAllPowers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

		ServerCommandSource serverCommandSource = context.getSource();
		Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "targets");
		List<Entity> nonLivingTargets = new ArrayList<>();
		List<LivingEntity> livingTargets = new ArrayList<>();
		List<LivingEntity> processedLivingTargets = new ArrayList<>();

		int clearedPowers = 0;
		for (Entity target : targets) {

			if (!(target instanceof LivingEntity livingTarget)) {
				nonLivingTargets.add(target);
				continue;
			}

			livingTargets.add(livingTarget);
			PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingTarget);
			Set<PowerType<?>> powerTypes = powerHolderComponent.getPowerTypes(false);

			for (PowerType<?> powerType : powerTypes) {
				List<Identifier> powerSources = powerHolderComponent.getSources(powerType);
				powerSources.forEach(powerHolderComponent::removeAllPowersFromSource);
			}

			if (!powerTypes.isEmpty()) powerHolderComponent.sync();
			clearedPowers += powerTypes.size();
			processedLivingTargets.add(livingTarget);

		}

		if (!processedLivingTargets.isEmpty()) {
			if (processedLivingTargets.size() == 1) serverCommandSource.sendFeedback(Text.translatable("commands.apoli.clear.success.single", processedLivingTargets.get(0).getDisplayName(), clearedPowers), true);
			else serverCommandSource.sendFeedback(Text.translatable("commands.apoli.clear.success.multiple", processedLivingTargets.size(), clearedPowers), true);
		}

		else if (!livingTargets.isEmpty()) {
			if (livingTargets.size() == 1) serverCommandSource.sendError(Text.translatable("commands.apoli.clear.fail.single", livingTargets.get(0).getDisplayName()));
			else serverCommandSource.sendError(Text.translatable("commands.apoli.clear.fail.multiple"));
		}

		else if (!nonLivingTargets.isEmpty()) {
			if (nonLivingTargets.size() == 1) serverCommandSource.sendError(Text.translatable("commands.apoli.non_living_entity", nonLivingTargets.get(0).getDisplayName()));
			else serverCommandSource.sendError(Text.translatable("commands.apoli.non_living_entities", nonLivingTargets.size()));
		}

		return clearedPowers;

	}

}
