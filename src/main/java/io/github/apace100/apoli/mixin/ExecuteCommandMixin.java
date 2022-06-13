package io.github.apace100.apoli.mixin;

import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.apace100.apoli.command.PowerTypeArgumentType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NumberRangeArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ExecuteCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;

@Mixin(ExecuteCommand.class)
public class ExecuteCommandMixin {

    private static final BinaryOperator<ResultConsumer<ServerCommandSource>> BINARY_RESULT_CONSUMER = (resultConsumer, resultConsumer2) -> (context, success, result) -> {
        resultConsumer.onCommandComplete(context, success, result);
        resultConsumer2.onCommandComplete(context, success, result);
    };

    @Inject(method = "Lnet/minecraft/server/command/ExecuteCommand;addStoreArguments(Lcom/mojang/brigadier/tree/LiteralCommandNode;Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;Z)Lcom/mojang/brigadier/builder/ArgumentBuilder;", at = @At("HEAD"))
    private static void addResourceArg(LiteralCommandNode<ServerCommandSource> node, LiteralArgumentBuilder<ServerCommandSource> builder, boolean requestResult, CallbackInfoReturnable<ArgumentBuilder<ServerCommandSource, ?>> cir) {
        builder.then(CommandManager.literal("resource").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(CommandManager.argument("power", PowerTypeArgumentType.power()).redirect(node, context -> executeStoreResource(context, requestResult)))));
    }

    @Inject(method = "addConditionArguments", at = @At("HEAD"))
    private static void addResourceConditionArg(CommandNode<ServerCommandSource> root, LiteralArgumentBuilder<ServerCommandSource> argumentBuilder, boolean positive, CallbackInfoReturnable<ArgumentBuilder<ServerCommandSource, ?>> cir) {
        argumentBuilder.then(CommandManager.literal("score").then(CommandManager.argument("target", EntityArgumentType.entity()).then(CommandManager.argument("power", PowerTypeArgumentType.power())
            .then(CommandManager.literal("=").then(CommandManager.literal("score").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> testResourceCondition(context, Integer::equals)))))
            .then(CommandManager.literal("<").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> testResourceCondition(context, (a, b) -> a < b)))))
            .then(CommandManager.literal("<=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> testResourceCondition(context, (a, b) -> a <= b)))))
            .then(CommandManager.literal(">").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> testResourceCondition(context, (a, b) -> a > b)))))
            .then(CommandManager.literal(">=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> testResourceCondition(context, (a, b) -> a >= b)))))
            .then(CommandManager.literal("matches").then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("range", NumberRangeArgumentType.intRange()), positive, context -> testResourceMatch(context, NumberRangeArgumentType.IntRangeArgumentType.getRangeArgument(context, "range")))))
        )))).then(CommandManager.literal("resource"));
    }

    private static boolean testResourceMatch(CommandContext<ServerCommandSource> context, NumberRange.IntRange range) throws CommandSyntaxException {
        Entity target = EntityArgumentType.getEntity(context, "target");
        PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
        if(target instanceof PlayerEntity player) {
            Power power = PowerHolderComponent.KEY.get(player).getPower(powerType);
            if (power instanceof VariableIntPower vp) {
                return range.test(vp.getValue());
            } else {
                return false;
            }
        }else {
            return false;
        }
    }

    private static boolean testResourceCondition(CommandContext<ServerCommandSource> context, BiPredicate<Integer, Integer> condition) throws CommandSyntaxException {
        Entity target = EntityArgumentType.getEntity(context, "target");
        PowerType<?> powerType = PowerTypeArgumentType.getPower(context, "power");
        if(target instanceof PlayerEntity player) {
            Power power = PowerHolderComponent.KEY.get(player).getPower(powerType);
            if (power instanceof VariableIntPower vp) {
                String string2 = ScoreHolderArgumentType.getScoreHolder(context, "source");
                ScoreboardObjective scoreboardObjective2 = ScoreboardObjectiveArgumentType.getObjective(context, "sourceObjective");
                ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
                if (!scoreboard.playerHasObjective(string2, scoreboardObjective2)) {
                    return false;
                }
                ScoreboardPlayerScore scoreboardPlayerScore2 = scoreboard.getPlayerScore(string2, scoreboardObjective2);
                return condition.test(vp.getValue(), scoreboardPlayerScore2.getScore());
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    private static ServerCommandSource executeStoreResource(CommandContext<ServerCommandSource> command, boolean requestResult) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgumentType.getEntities(command, "targets");
        PowerType<?> powerType = PowerTypeArgumentType.getPower(command, "power");
        return command.getSource().mergeConsumers((context, success, result) -> {
            for (Entity entity : targets) {
                if(entity instanceof PlayerEntity player) {
                    Power power = PowerHolderComponent.KEY.get(player).getPower(powerType);
                    if (power instanceof VariableIntPower vp) {
                        int i = requestResult ? result : (success ? 1 : 0);
                        vp.setValue(i);
                        PowerHolderComponent.syncPower(player, powerType);
                    }
                }
            }
        }, BINARY_RESULT_CONSUMER);
    }
}
