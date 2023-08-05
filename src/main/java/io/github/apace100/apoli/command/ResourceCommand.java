package io.github.apace100.apoli.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ResourceCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("resource").requires(cs -> cs.hasPermissionLevel(2))
                .then(literal("has")
                    .then(argument("target", EntityArgumentType.entity())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .executes((command) -> resource(command, SubCommand.HAS))))
                )
                .then(literal("get")
                    .then(argument("target", EntityArgumentType.entity())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .executes((command) -> resource(command, SubCommand.GET))))
                )
                .then(literal("set")
                    .then(argument("target", EntityArgumentType.entity())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .then(argument("value", IntegerArgumentType.integer())
                                .executes((command) -> resource(command, SubCommand.SET)))))
                )
                .then(literal("change")
                    .then(argument("target", EntityArgumentType.entity())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .then(argument("value", IntegerArgumentType.integer())
                                .executes((command) -> resource(command, SubCommand.CHANGE)))))
                )
                .then(literal("operation")
                    .then(argument("target", EntityArgumentType.entity())
                        .then(argument("power", PowerTypeArgumentType.power())
                            .then(argument("operation", PowerOperation.operation())
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
        Entity target = EntityArgumentType.getEntity(context, "target");
        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(target)
            .orElse(null);

        if (component == null) {
            source.sendError(Text.translatable("commands.apoli.reosurce.invalid_entity"));
            return 0;
        }

        PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
        Power power = component.getPower(powerType);

        return switch (subCommand) {
            case HAS -> {
                source.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), true);
                yield  1;
            }
            case GET -> {

                int value = getValue(power);

                source.sendFeedback(() -> Text.translatable("commands.scoreboard.players.get.success", target.getName().getString(), value, powerType.getIdentifier()), true);
                yield value;

            }
            case SET -> {

                int value = IntegerArgumentType.getInteger(context, "value");
                setValue(power, value);

                PowerHolderComponent.syncPower(target, powerType);
                source.sendFeedback(() -> Text.translatable("commands.scoreboard.players.set.success.single", powerType.getIdentifier(), target.getName().getString(), value), true);

                yield 1;

            }
            case CHANGE -> {

                int value = IntegerArgumentType.getInteger(context, "value");
                int total = getValue(power) + value;

                setValue(power, total);
                PowerHolderComponent.syncPower(target, powerType);

                source.sendFeedback(() -> Text.translatable("commands.scoreboard.players.add.success.single", value, powerType.getIdentifier(), target.getName().getString(), total), true);
                yield 1;

            }
            case OPERATION -> {

                String scoreHolder = ScoreHolderArgumentType.getScoreHolder(context, "entity");
                ScoreboardObjective scoreboardObjective = ScoreboardObjectiveArgumentType.getObjective(context, "objective");

                ScoreboardPlayerScore scoreboardPlayerScore = source.getServer().getScoreboard().getPlayerScore(scoreHolder, scoreboardObjective);
                context.getArgument("operation", PowerOperation.Operation.class).apply(power, scoreboardPlayerScore);

                int value = getValue(power);
                source.sendFeedback(() -> Text.translatable("commands.scoreboard.players.operation.success.single", powerType.getIdentifier(), target.getName().getString(), value), true);

                yield 1;

            }
        };

    }

    private static int getValue(Power power) {
        if (power instanceof VariableIntPower vip) {
            return vip.getValue();
        } else if (power instanceof CooldownPower cp) {
            return cp.getRemainingTicks();
        } else {
            return 0;
        }
    }

    private static void setValue(Power power, int newValue) {
        if (power instanceof VariableIntPower vip) {
            vip.setValue(newValue);
        } else if (power instanceof CooldownPower cp) {
            cp.setCooldown(newValue);
        }
    }

}