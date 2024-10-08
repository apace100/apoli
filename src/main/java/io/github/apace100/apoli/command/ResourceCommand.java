package io.github.apace100.apoli.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.apace100.apoli.command.argument.PowerArgumentType;
import io.github.apace100.apoli.command.argument.PowerHolderArgumentType;
import io.github.apace100.apoli.command.argument.PowerOperationArgumentType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.PowerUtil;
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
                        .then(argument("power", PowerArgumentType.power())
                            .executes((command) -> resource(command, SubCommand.HAS))))
                )
                .then(literal("get")
                    .then(argument("target", PowerHolderArgumentType.holder())
                        .then(argument("power", PowerArgumentType.power())
                            .executes((command) -> resource(command, SubCommand.GET))))
                )
                .then(literal("set")
                    .then(argument("target", PowerHolderArgumentType.holder())
                        .then(argument("power", PowerArgumentType.power())
                            .then(argument("value", IntegerArgumentType.integer())
                                .executes((command) -> resource(command, SubCommand.SET)))))
                )
                .then(literal("change")
                    .then(argument("target", PowerHolderArgumentType.holder())
                        .then(argument("power", PowerArgumentType.power())
                            .then(argument("value", IntegerArgumentType.integer())
                                .executes((command) -> resource(command, SubCommand.CHANGE)))))
                )
                .then(literal("operation")
                    .then(argument("target", PowerHolderArgumentType.holder())
                        .then(argument("power", PowerArgumentType.power())
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

        Power power = PowerArgumentType.getPower(context, "power");
        PowerType powerType = power.getType(target);

        if (powerType == null) {
            source.sendError(Text.translatable("commands.apoli.resource.fail", target.getName(), power.getId().toString()));
            return 0;
        }

        return switch (subCommand) {
            case HAS -> {

                if (PowerUtil.validateResource(powerType).isSuccess()) {
                    source.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), false);
                    yield 1;
                }

                else {
                    source.sendError(Text.translatable("commands.execute.conditional.fail"));
                    yield 0;
                }

            }
            case GET -> {

                if (PowerUtil.validateResource(powerType).isSuccess()) {

                    int value = PowerUtil.getResourceValue(powerType);
                    source.sendFeedback(() -> Text.translatable("commands.scoreboard.players.get.success", target.getName(), value, power.getId().toString()), false);

                    yield value;

                }

                else {
                    source.sendError(Text.translatable("commands.scoreboard.players.get.null", power.getId().toString(), target.getName()));
                    yield 0;
                }

            }
            case SET -> {

                powerType = PowerUtil
                    .validateResource(powerType)
                    .getOrThrow(err -> new SimpleCommandExceptionType(Text.translatableWithFallback("commands.apoli.resource.set.fail", err, power.getId().toString(), power.getFactoryInstance().getSerializerId().toString())).create());

                int value = IntegerArgumentType.getInteger(context, "value");
                if (PowerUtil.setResourceValue(powerType, value)) {
                    PowerHolderComponent.syncPower(target, power);
                }

                source.sendFeedback(() -> Text.translatable("commands.scoreboard.players.set.success.single", power.getId().toString(), target.getName(), value), true);
                yield PowerUtil.getResourceValue(powerType);

            }
            case CHANGE -> {

                powerType = PowerUtil
                    .validateResource(powerType)
                    .getOrThrow(err -> new SimpleCommandExceptionType(Text.translatableWithFallback("commands.apoli.resource.change.fail", err, power.getId().toString(), power.getFactoryInstance().getSerializerId().toString())).create());

                int value = IntegerArgumentType.getInteger(context, "value");
                int newValue = PowerUtil.getResourceValue(powerType);

                if (PowerUtil.changeResourceValue(powerType, value)) {
                    PowerHolderComponent.syncPower(target, power);
                }

                source.sendFeedback(() -> Text.translatable("commands.scoreboard.players.add.success.single", value, power.getId().toString(), target.getName(), newValue), true);
                yield PowerUtil.getResourceValue(powerType);

            }
            case OPERATION -> {

                powerType = PowerUtil
                    .validateResource(powerType)
                    .getOrThrow(err -> new SimpleCommandExceptionType(Text.translatableWithFallback("commands.apoli.resource.operation.fail", err, power.getId().toString(), power.getFactoryInstance().getSerializerId().toString())).create());

                ScoreHolder scoreHolder = ScoreHolderArgumentType.getScoreHolder(context, "entity");
                ScoreboardObjective scoreboardObjective = ScoreboardObjectiveArgumentType.getObjective(context, "objective");

                ScoreAccess scoreAccess = source.getServer().getScoreboard().getOrCreateScore(scoreHolder, scoreboardObjective);
                PowerOperationArgumentType.Operation operation = PowerOperationArgumentType.getOperation(context, "operation");

                boolean modified = operation.apply(powerType, scoreAccess);
                int newValue = PowerUtil.getResourceValue(powerType);

                if (modified) {
                    PowerHolderComponent.syncPower(target, power);
                }

                source.sendFeedback(() -> Text.translatable("commands.scoreboard.players.operation.success.single", power.getId().toString(), target.getName(), newValue), true);
                yield newValue;

            }

        };

    }

}
