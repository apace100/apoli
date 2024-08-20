package io.github.apace100.apoli.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.command.argument.PowerHolderArgumentType;
import io.github.apace100.apoli.command.argument.PowerOperationArgumentType;
import io.github.apace100.apoli.command.argument.PowerTypeArgumentType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.CooldownPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.VariableIntPowerType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ResourceCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("resource").requires(cs -> cs.hasPermissionLevel(2))
                .then(literal("has")
                    .then(argument("target", PowerHolderArgumentType.holder())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .executes((command) -> resource(command, SubCommand.HAS))))
                )
                .then(literal("get")
                    .then(argument("target", PowerHolderArgumentType.holder())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .executes((command) -> resource(command, SubCommand.GET))))
                )
                .then(literal("set")
                    .then(argument("target", PowerHolderArgumentType.holder())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .then(argument("value", IntegerArgumentType.integer())
                                .executes((command) -> resource(command, SubCommand.SET)))))
                )
                .then(literal("change")
                    .then(argument("target", PowerHolderArgumentType.holder())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .then(argument("value", IntegerArgumentType.integer())
                                .executes((command) -> resource(command, SubCommand.CHANGE)))))
                )
                .then(literal("operation")
                    .then(argument("target", PowerHolderArgumentType.holder())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .then(argument("operation", PowerOperationArgumentType.operation())
                                .then(argument("entity", ScoreHolderArgumentType.scoreHolder())
                                    .then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                        .executes((command) -> resource(command, SubCommand.OPERATION)))))))
                )
        );
    }

    public enum SubCommand {
        HAS, GET, SET, CHANGE, OPERATION
    }

    // This is a cleaner method than sticking it into every subcommand
    private static int resource(CommandContext<ServerCommandSource> context, SubCommand subCommand) throws CommandSyntaxException {

        ServerCommandSource source = context.getSource();
        LivingEntity target = PowerHolderArgumentType.getHolder(context, "target");
        PowerHolderComponent component = PowerHolderComponent.KEY.get(target);

        Power power = PowerTypeArgumentType.getPower(context, "power");
        PowerType powerType = component.getPowerType(power);

        return switch (subCommand) {
            case HAS -> {

                if (isPowerInvalid(powerType)) {
                    source.sendError(Text.stringifiedTranslatable("commands.execute.conditional.fail"));
                    yield 0;
                }

                source.sendFeedback(() -> Text.stringifiedTranslatable("commands.execute.conditional.pass"), true);
                yield  1;

            }
            case GET -> {

                if (isPowerInvalid(powerType)) {
                    source.sendError(Text.stringifiedTranslatable("commands.scoreboard.players.get.null", power.getId(), target.getName().getString()));
                    yield 0;
                }

                int value = getValue(powerType);
                source.sendFeedback(() -> Text.stringifiedTranslatable("commands.scoreboard.players.get.success", target.getName().getString(), value, power.getId()), true);

                yield value;

            }
            case SET -> {

                if (isPowerInvalid(powerType)) {
                    source.sendError(Text.stringifiedTranslatable("argument.scoreHolder.empty"));
                    yield 0;
                }

                int value = IntegerArgumentType.getInteger(context, "value");
                setValue(powerType, value);

                PowerHolderComponent.syncPower(target, power);
                source.sendFeedback(() -> Text.stringifiedTranslatable("commands.scoreboard.players.set.success.single", power.getId(), target.getName().getString(), value), true);

                yield value;

            }
            case CHANGE -> {

                if (isPowerInvalid(powerType)) {
                    source.sendError(Text.stringifiedTranslatable("argument.scoreHolder.empty"));
                    yield 0;
                }

                int value = IntegerArgumentType.getInteger(context, "value");
                int total = getValue(powerType) + value;

                setValue(powerType, total);
                PowerHolderComponent.syncPower(target, power);

                source.sendFeedback(() -> Text.stringifiedTranslatable("commands.scoreboard.players.add.success.single", value, power.getId(), target.getName().getString(), total), true);
                yield total;

            }
            case OPERATION -> {

                if (isPowerInvalid(powerType)) {
                    source.sendError(Text.stringifiedTranslatable("argument.scoreHolder.empty"));
                    yield 0;
                }

                ScoreHolder scoreHolder = ScoreHolderArgumentType.getScoreHolder(context, "entity");
                ScoreboardObjective scoreboardObjective = ScoreboardObjectiveArgumentType.getObjective(context, "objective");

                ScoreAccess scoreAccess = source.getServer().getScoreboard().getOrCreateScore(scoreHolder, scoreboardObjective);
                context.getArgument("operation", PowerOperationArgumentType.Operation.class).apply(powerType, scoreAccess);

                int value = getValue(powerType);
                source.sendFeedback(() -> Text.stringifiedTranslatable("commands.scoreboard.players.operation.success.single", power.getId(), target.getName().getString(), value), true);

                yield value;

            }
        };

    }

    private static int getValue(PowerType powerType) {
        if (powerType instanceof VariableIntPowerType vip) {
            return vip.getValue();
        } else if (powerType instanceof CooldownPowerType cp) {
            return cp.getRemainingTicks();
        } else {
            return 0;
        }
    }

    private static void setValue(PowerType powerType, int newValue) {
        if (powerType instanceof VariableIntPowerType vip) {
            vip.setValue(newValue);
        } else if (powerType instanceof CooldownPowerType cp) {
            cp.setCooldown(newValue);
        }
    }

    private static boolean isPowerInvalid(PowerType powerType) {
        return !(powerType instanceof VariableIntPowerType) && !(powerType instanceof CooldownPowerType);
    }

}
