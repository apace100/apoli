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
import net.minecraft.entity.LivingEntity;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Optional;

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
    private static int resource(CommandContext<ServerCommandSource> command, SubCommand sub) throws CommandSyntaxException {
        Entity player = EntityArgumentType.getEntity(command, "target");
        if(!(player instanceof LivingEntity)) {
        }
        PowerType<?> powerType = PowerTypeArgumentType.getPower(command, "power");
        Optional<PowerHolderComponent> phc = PowerHolderComponent.KEY.maybeGet(player);
        if(phc.isEmpty()) {
            command.getSource().sendError(Text.translatable("commands.apoli.resource.invalid_entity"));
            return 0;
        }
        Power power = PowerHolderComponent.KEY.get(player).getPower(powerType);

        if (power instanceof VariableIntPower vIntPower) {
            switch (sub)
            {
                case HAS ->
                {
                    command.getSource().sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), true);
                    return 1;
                }
                case GET ->
                {
                    int i = vIntPower.getValue();
                    command.getSource().sendFeedback(() -> Text.translatable("commands.scoreboard.players.get.success", player.getEntityName(), i, powerType.getIdentifier()), true);
                    return i;
                }
                case SET ->
                {
                    int i = IntegerArgumentType.getInteger(command, "value");
                    vIntPower.setValue(i);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendFeedback(() -> Text.translatable("commands.scoreboard.players.set.success.single", powerType.getIdentifier(), player.getEntityName(), i), true);
                    return 1;
                }
                case CHANGE ->
                {
                    int i = IntegerArgumentType.getInteger(command, "value");
                    int total = vIntPower.getValue() + i;
                    vIntPower.setValue(total);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendFeedback(() -> Text.translatable("commands.scoreboard.players.add.success.single", i, powerType.getIdentifier(), player.getEntityName(), total), true);
                    return 1;
                }
                case OPERATION ->
                {
                    ScoreboardPlayerScore score = command.getSource().getServer().getScoreboard().getPlayerScore(ScoreHolderArgumentType.getScoreHolder(command, "entity"), ScoreboardObjectiveArgumentType.getObjective(command, "objective"));
                    command.getArgument("operation", PowerOperation.Operation.class).apply(vIntPower, score);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendFeedback(() -> Text.translatable("commands.scoreboard.players.operation.success.single", powerType.getIdentifier(), player.getEntityName(), vIntPower.getValue()), true);
                    return 1;
                }
            }
        } else if(power instanceof CooldownPower cooldownPower) {
            switch (sub)
            {
                case HAS ->
                {
                    command.getSource().sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), true);
                    return 1;
                }
                case GET ->
                {
                    int i = cooldownPower.getRemainingTicks();
                    command.getSource().sendFeedback(() -> Text.translatable("commands.scoreboard.players.get.success", player.getEntityName(), i, powerType.getIdentifier()), true);
                    return i;
                }
                case SET ->
                {
                    int i = IntegerArgumentType.getInteger(command, "value");
                    cooldownPower.setCooldown(i);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendFeedback(() -> Text.translatable("commands.scoreboard.players.set.success.single", powerType.getIdentifier(), player.getEntityName(), i), true);
                    return 1;
                }
                case CHANGE ->
                {
                    int i = IntegerArgumentType.getInteger(command, "value");
                    cooldownPower.modify(i);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendFeedback(() -> Text.translatable("commands.scoreboard.players.add.success.single", i, powerType.getIdentifier(), player.getEntityName(), cooldownPower.getRemainingTicks()), true);
                    return 1;
                }
                case OPERATION ->
                {
                    ScoreboardPlayerScore score = command.getSource().getServer().getScoreboard().getPlayerScore(ScoreHolderArgumentType.getScoreHolder(command, "entity"), ScoreboardObjectiveArgumentType.getObjective(command, "objective"));
                    command.getArgument("operation", PowerOperation.Operation.class).apply(cooldownPower, score);
                    PowerHolderComponent.syncPower(player, powerType);
                    command.getSource().sendFeedback(() -> Text.translatable("commands.scoreboard.players.operation.success.single", powerType.getIdentifier(), player.getEntityName(), cooldownPower.getRemainingTicks()), true);
                    return 1;
                }
            }
        } else {
            switch (sub)
            {
                case HAS ->
                {
                    command.getSource().sendError(Text.translatable("commands.execute.conditional.fail"));
                    return 0;
                }
                case GET ->
                {
                    command.getSource().sendError(Text.translatable("commands.scoreboard.players.get.null", powerType.getIdentifier(), player.getEntityName()));
                    return 0;
                }
                case SET, CHANGE, OPERATION ->
                {
                    // This translation is a bit of a stretch, as it reads "No relevant score holders could be found"
                    command.getSource().sendError(Text.translatable("argument.scoreHolder.empty"));
                    return 0;
                }
            }
        }
        return 0;
    }
}