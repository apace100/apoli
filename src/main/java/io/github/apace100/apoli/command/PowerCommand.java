package io.github.apace100.apoli.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PowerCommand {

	public static final Identifier COMMAND_POWER_SOURCE = Apoli.identifier("command");

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// TODO: Clean up this mess.
		dispatcher.register(
			literal("power").requires(cs -> cs.hasPermissionLevel(2))
				.then(literal("grant")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes((command) -> {
									int i = 0;
									try {

										Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
										PowerType<?> power = PowerTypeArgumentType.getPower(command, "power");
										for(Entity target : targets) {
											if(target instanceof LivingEntity) {
												if(grantPower((LivingEntity)target, power)) {
													i++;
												}
											}
										}
										if(i == 0) {
											if(targets.size() == 1) {
												command.getSource().sendError(Text.translatable("commands.apoli.grant.fail.single", targets.iterator().next().getDisplayName(), power.getName(), COMMAND_POWER_SOURCE));
											} else {
												command.getSource().sendError(Text.translatable("commands.apoli.grant.fail.multiple", targets.size(), power.getName(), COMMAND_POWER_SOURCE));
											}
										} else
										if (targets.size() == 1 && i == 1) {
											command.getSource().sendFeedback(Text.translatable("commands.apoli.grant.success.single", targets.iterator().next().getDisplayName(), power.getName()), true);
										} else {
											command.getSource().sendFeedback(Text.translatable("commands.apoli.grant.success.multiple", i, power.getName()), true);
										}
									} catch (Exception e) {
										command.getSource().sendError(Text.literal(e.getMessage()));
									}
									return i;
								}
							)
							.then(argument("source", IdentifierArgumentType.identifier())
								.executes((command) -> {
									int i = 0;
									try {
										Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
										PowerType<?> power = PowerTypeArgumentType.getPower(command, "power");
										Identifier source = IdentifierArgumentType.getIdentifier(command, "source");
										for(Entity target : targets) {
											if(target instanceof LivingEntity) {
												if(grantPower((LivingEntity)target, power, source)) {
													i++;
												}
											}
										}
										if(i == 0) {
											if(targets.size() == 1) {
												command.getSource().sendError(Text.translatable("commands.apoli.grant.fail.single", targets.iterator().next().getDisplayName(), power.getName(), source));
											} else {
												command.getSource().sendError(Text.translatable("commands.apoli.grant.fail.multiple", targets.size(), power.getName(), source));
											}
										} else
										if (targets.size() == 1 && i == 1) {
											command.getSource().sendFeedback(Text.translatable("commands.apoli.grant_from_source.success.single", targets.iterator().next().getDisplayName(), power.getName(), source), true);
										} else {
											command.getSource().sendFeedback(Text.translatable("commands.apoli.grant_from_source.success.multiple", i, power.getName(), source), true);
										}
									} catch (Exception e) {
										command.getSource().sendError(Text.literal(e.getMessage()));
									}
									return i;
								})))))
				.then(literal("revoke")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes((command) -> {
								int i = 0;
								Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
								PowerType<?> power = PowerTypeArgumentType.getPower(command, "power");
								try {
									for (Entity target : targets) {
										if (target instanceof LivingEntity) {
											if (revokePower((LivingEntity) target, power)) {
												i++;
											}
										}
									}

									if (i == 0) {
										if(targets.size() == 1) {
											command.getSource().sendError(Text.translatable("commands.apoli.revoke.fail.single", targets.iterator().next().getDisplayName(), power.getName(), COMMAND_POWER_SOURCE));
										} else {
											command.getSource().sendError(Text.translatable("commands.apoli.revoke.fail.multiple", power.getName(), COMMAND_POWER_SOURCE));
										}
									} else if (targets.size() == 1) {
										command.getSource().sendFeedback(Text.translatable("commands.apoli.revoke.success.single", targets.iterator().next().getDisplayName(), power.getName()), false);
									} else {
										command.getSource().sendFeedback(Text.translatable("commands.apoli.revoke.success.multiple", i, power.getName()), false);
									}
								} catch (Exception e) {
									command.getSource().sendError(Text.literal(e.getMessage()));
								}
								return i;
							})
							.then(argument("source", IdentifierArgumentType.identifier())
								.executes((command) -> {
									int i = 0;
									Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
									PowerType<?> power = PowerTypeArgumentType.getPower(command, "power");
									Identifier source = IdentifierArgumentType.getIdentifier(command, "source");
									try {
										for (Entity target : targets) {
											if (target instanceof LivingEntity) {
												if (revokePower((LivingEntity) target, power, source)) {
													i++;
												}
											}
										}

										if (i == 0) {
											if(targets.size() == 1) {
												command.getSource().sendError(Text.translatable("commands.apoli.revoke.fail.single", targets.iterator().next().getDisplayName(), power.getName(), source));
											} else {
												command.getSource().sendError(Text.translatable("commands.apoli.revoke.fail.multiple", power.getName(), source));
											}
										} else if (targets.size() == 1) {
											command.getSource().sendFeedback(Text.translatable("commands.apoli.revoke_from_source.success.single", targets.iterator().next().getDisplayName(), power.getName(), source), false);
										} else {
											command.getSource().sendFeedback(Text.translatable("commands.apoli.revoke_from_source.success.multiple", i, power.getName(), source), false);
										}
									} catch (Exception e) {
										command.getSource().sendError(Text.literal(e.getMessage()));
									}
									return i;
								})))))
				.then(literal("revokeall")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("source", IdentifierArgumentType.identifier())
							.executes((command) -> {
								int i = 0;
								Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
								Identifier source = IdentifierArgumentType.getIdentifier(command, "source");
								try {
									for (Entity target : targets) {
										if (target instanceof LivingEntity) {
											i += revokeAllPowersFromSource((LivingEntity) target, source);
										}
									}

									if (i == 0) {
										if(targets.size() == 1) {
											command.getSource().sendError(Text.translatable("commands.apoli.revoke_all.fail.single", targets.iterator().next().getDisplayName(), source));
										} else {
											command.getSource().sendError(Text.translatable("commands.apoli.revoke_all.fail.multiple", source));
										}
									} else if (targets.size() == 1) {
										command.getSource().sendFeedback(Text.translatable("commands.apoli.revoke_all.success.single", targets.iterator().next().getDisplayName(), i, source), false);
									} else {
										command.getSource().sendFeedback(Text.translatable("commands.apoli.revoke_all.success.multiple", targets.size(), i, source), false);
									}
								} catch (Exception e) {
									command.getSource().sendError(Text.literal(e.getMessage()));
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
								for(PowerType<?> powerType : component.getPowerTypes(false)) {
									if(i > 0)
										powers.append(", ");
									powers.append(powerType.getIdentifier().toString());
									i++;
								}
								command.getSource().sendFeedback(Text.translatable("commands.apoli.list.pass", i, powers), false);
							} else {
								command.getSource().sendError(Text.translatable("commands.apoli.list.fail"));
							}
							return i;
						})
						.then(argument("subpowers", BoolArgumentType.bool())
							.executes((command) -> {
								int i = 0;
								Entity target = EntityArgumentType.getEntity(command, "target");
								boolean listSubpowers = BoolArgumentType.getBool(command, "subpowers");
								if(target instanceof LivingEntity) {
									PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
									StringBuilder powers = new StringBuilder();
									for(PowerType<?> powerType : component.getPowerTypes(listSubpowers)) {
										if(i > 0)
											powers.append(", ");
										powers.append(powerType.getIdentifier().toString());
										i++;
									}
									command.getSource().sendFeedback(Text.translatable("commands.apoli.list.pass", i, powers), false);
								} else {
									command.getSource().sendError(Text.translatable("commands.apoli.list.fail"));
								}
								return i;
							}))))
				.then(literal("has")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes((command) -> {
								int i = 0;
								Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
								PowerType<?> power = PowerTypeArgumentType.getPower(command, "power");
								for(Entity target : targets) {
									if(target instanceof LivingEntity) {
										if (hasPower((LivingEntity)target, power)) {
											i++;
										}
									}
								}
								if (i == 0) {
									command.getSource().sendError(Text.translatable("commands.execute.conditional.fail"));
								} else if (targets.size() == 1) {
									command.getSource().sendFeedback(Text.translatable("commands.execute.conditional.pass"), false);
								} else {
									command.getSource().sendFeedback(Text.translatable("commands.execute.conditional.pass_count", i), false);
								}
								return i;
							}))))
				.then(literal("sources")
					.then(argument("target", EntityArgumentType.entity())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes((command) -> {
								int i = 0;
								Entity target = EntityArgumentType.getEntity(command, "target");
								PowerType<?> power = PowerTypeArgumentType.getPower(command, "power");
								if(target instanceof LivingEntity) {
									PowerHolderComponent component = PowerHolderComponent.KEY.get(target);
									StringBuilder sources = new StringBuilder();
									for(Identifier source : component.getSources(power)) {
										if(i > 0)
											sources.append(", ");
										sources.append(source.toString());
										i++;
									}
									command.getSource().sendFeedback(Text.translatable("commands.apoli.sources.pass", target.getDisplayName(), i, power.getName(), sources), false);
								} else {
									command.getSource().sendError(Text.translatable("commands.apoli.sources.fail", target.getDisplayName(), power.getName()));
								}
								return i;
							}))))
				.then(literal("remove")
					.then(argument("targets", EntityArgumentType.entities())
						.then(argument("power", PowerTypeArgumentType.power())
							.executes((command) -> {
								int i = 0;
								Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
								PowerType<?> power = PowerTypeArgumentType.getPower(command, "power");
								try {
									for (Entity target : targets) {
										if (target instanceof LivingEntity) {
											if (revokePowerAllSources((LivingEntity) target, power)) {
												i++;
											}
										}
									}

									if (i == 0) {
										if(targets.size() == 1) {
											command.getSource().sendError(Text.translatable("commands.apoli.remove.fail.single", targets.iterator().next().getDisplayName(), power.getName()));
										} else {
											command.getSource().sendError(Text.translatable("commands.apoli.remove.fail.multiple", power.getName()));
										}
									} else if (targets.size() == 1) {
										command.getSource().sendFeedback(Text.translatable("commands.apoli.remove.success.single", targets.iterator().next().getDisplayName(), power.getName()), false);
									} else {
										command.getSource().sendFeedback(Text.translatable("commands.apoli.remove.success.multiple", i, power.getName()), false);
									}
								} catch (Exception e) {
									command.getSource().sendError(Text.literal(e.getMessage()));
								}
								return i;
							}))))
				.then(literal("clear")
					.then(argument("targets", EntityArgumentType.entities())
						.executes((command) -> {
							int i = 0;
							Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
							try {
								for (Entity target : targets) {
									if (target instanceof LivingEntity) {
										i += revokeAllPowers((LivingEntity) target);
									}
								}

								if (i == 0) {
									if(targets.size() == 1) {
										command.getSource().sendError(Text.translatable("commands.apoli.clear.fail.single", targets.iterator().next().getDisplayName()));
									} else {
										command.getSource().sendError(Text.translatable("commands.apoli.clear.fail.multiple"));
									}
								} else if (targets.size() == 1) {
									command.getSource().sendFeedback(Text.translatable("commands.apoli.clear.success.single", targets.iterator().next().getDisplayName(), i), false);
								} else {
									command.getSource().sendFeedback(Text.translatable("commands.apoli.clear.success.multiple", targets.size(), i), false);
								}
							} catch (Exception e) {
								command.getSource().sendError(Text.literal(e.getMessage()));
							}
							return i;
						}))));

	}

	private static boolean grantPower(LivingEntity entity, PowerType<?> power) {
		return grantPower(entity, power, COMMAND_POWER_SOURCE);
	}

	private static boolean grantPower(LivingEntity entity, PowerType<?> power, Identifier source) {
		PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
		boolean success = component.addPower(power, source);
		if(success) {
			component.sync();
			return true;
		}
		return false;
	}

	private static boolean revokePower(LivingEntity entity, PowerType<?> power) {
		return revokePower(entity, power, COMMAND_POWER_SOURCE);
	}

	private static boolean revokePower(LivingEntity entity, PowerType<?> power, Identifier source) {
		PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
		if(component.hasPower(power, source)) {
			component.removePower(power, source);
			component.sync();
			return true;
		}
		return false;
	}

	private static boolean revokePowerAllSources(LivingEntity entity, PowerType<?> power) {
		PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
		List<Identifier> sources = component.getSources(power);
		for(Identifier source : sources) {
			component.removePower(power, source);
		}
		if(sources.size() > 0) {
			component.sync();
		}
		return true;
	}

	private static int revokeAllPowers(LivingEntity entity) {
		PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
		Set<PowerType<?>> powers = component.getPowerTypes(false);
		for(PowerType<?> power : powers) {
			revokePowerAllSources(entity, power);
		}
		if(powers.size() > 0) {
			component.sync();
		}
		return powers.size();
	}

	private static int revokeAllPowersFromSource(LivingEntity entity, Identifier source) {
		PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
		int i = component.removeAllPowersFromSource(source);
		if(i > 0) {
			component.sync();
		}
		return i;
	}

	private static boolean hasPower(LivingEntity entity, PowerType<?> powerType) {
		return PowerHolderComponent.KEY.get(entity).hasPower(powerType);
	}
}
