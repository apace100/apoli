package io.github.apace100.apoli.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PowerCommand {

	public static final Identifier COMMAND_POWER_SOURCE = Apoli.identifier("command");

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			literal("power").requires(cs -> cs.hasPermissionLevel(2))
				.then(literal("grant")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("power", PowerArgument.power())
							.executes((command) -> {
									int i = 0;
								try {

									Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
									PowerType<?> power = command.getArgument("power", PowerType.class);
									for(Entity target : targets) {
										if(target instanceof LivingEntity) {
											grantPower((LivingEntity)target, power);
											i++;
										}
									}
									if (targets.size() == 1 && i == 1) {
										command.getSource().sendFeedback(new TranslatableText("commands.apoli.grant.success.single", targets.iterator().next().getDisplayName(), power.getName()), true);
									} else {
										command.getSource().sendFeedback(new TranslatableText("commands.apoli.grant.success.multiple", i, power.getName()), true);
									}
								} catch (Exception e) {
									Apoli.LOGGER.error(e.getClass().getSimpleName() + ":");
									e.printStackTrace();
									command.getSource().sendError(new LiteralText(e.getMessage()));
								}
								return i;
							}
							)
						)
					)
				)
			.then(literal("revoke")
				.then(argument("targets", EntityArgumentType.entities())
					.then(argument("power", PowerArgument.power())
						.executes((command) -> {
							int i = 0;
							Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
							PowerType<?> power = command.getArgument("power", PowerType.class);
							for(Entity target : targets) {
								if(target instanceof LivingEntity) {
									if (revokePower((LivingEntity)target, power)) {
										i++;
									}
								}
							}
							if (i == 0) {
								command.getSource().sendError(new TranslatableText("commands.apoli.revoke.fail"));
							} else if (targets.size() == 1) {
								command.getSource().sendFeedback(new TranslatableText("commands.apoli.revoke.success.single", targets.iterator().next().getDisplayName(), power.getName()), false);
							} else {
								command.getSource().sendFeedback(new TranslatableText("commands.apoli.revoke.success.multiple", i, power.getName()), false);
							}
							return i;
						}))))

			.then(literal("list")
				.then(argument("target", EntityArgumentType.entity())
					.executes((command) -> {
							int i = 0;
							Entity target = EntityArgumentType.getEntity(command, "target");
							if(target instanceof LivingEntity) {
								PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
								StringBuilder powers = new StringBuilder();
								for(PowerType<?> powerType : component.getPowerTypes()) {
									if(i > 0)
										powers.append(", ");
									powers.append(powerType.getIdentifier().toString());
									i++;
								}
								command.getSource().sendFeedback(new TranslatableText("commands.apoli.list.pass", i, powers), false);
							} else {
								command.getSource().sendError(new TranslatableText("commands.apoli.list.fail"));
							}
							return i;
						})))
				.then(literal("has")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("power", PowerArgument.power())
						.executes((command) -> {
							int i = 0;
							Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
							PowerType<?> power = command.getArgument("power", PowerType.class);
							for(Entity target : targets) {
								if(target instanceof LivingEntity) {
									if (hasPower((LivingEntity)target, power)) {
										i++;
									}
								}
							}
							if (i == 0) {
								command.getSource().sendError(new TranslatableText("commands.execute.conditional.fail"));
							} else if (targets.size() == 1) {
								command.getSource().sendFeedback(new TranslatableText("commands.execute.conditional.pass"), false);
							} else {
								command.getSource().sendFeedback(new TranslatableText("commands.execute.conditional.pass_count", i), false);
							}
							return i;
						})))));

	}

	private static void grantPower(LivingEntity entity, PowerType<?> power) {
		PowerHolderComponent.KEY.get(entity).addPower(power, COMMAND_POWER_SOURCE);
		PowerHolderComponent.sync(entity);
	}

	private static boolean revokePower(LivingEntity entity, PowerType<?> power) {
		PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
		if(component.hasPower(power, COMMAND_POWER_SOURCE)) {
			component.removePower(power, COMMAND_POWER_SOURCE);
			PowerHolderComponent.sync(entity);
			return true;
		}
		return false;
	}

	private static boolean hasPower(LivingEntity entity, PowerType<?> powerType) {
		return PowerHolderComponent.KEY.get(entity).hasPower(powerType);
	}
}
