package io.github.apace100.apoli.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import io.github.apace100.apoli.command.argument.PowerArgumentType;
import io.github.apace100.apoli.command.argument.PowerHolderArgumentType;
import io.github.apace100.apoli.command.argument.PowerOperationArgumentType;
import io.github.apace100.apoli.command.argument.suggestion.PowerSuggestionProvider;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.PowerUtil;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ResourceCommand {

    public static void register(CommandNode<ServerCommandSource> baseNode) {

        //  The main node of the command
        var resourceNode = literal("resource")
            .requires(source -> source.hasPermissionLevel(2))
            .build();

        //  Add the sub-nodes as children of the main node
        resourceNode.addChild(HasNode.get());
        resourceNode.addChild(GetNode.get());
        resourceNode.addChild(SetNode.get());
        resourceNode.addChild(ChangeNode.get());
        resourceNode.addChild(OperationNode.get());

        //  Add the main node as a child of the base node
        baseNode.addChild(resourceNode);

    }

    public static class HasNode {

        public static CommandNode<ServerCommandSource> get() {
            return literal("has")
                .then(argument("target", PowerHolderArgumentType.holder())
                    .then(argument("resource", PowerArgumentType.resource())
                        .executes(HasNode::execute))).build();
        }

        public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

            Entity target = PowerHolderArgumentType.getHolder(context, "target");
            Power power = PowerArgumentType.getResource(context, "resource");

            ServerCommandSource commandSource = context.getSource();
            PowerType powerType = power.getType(target);

            if (powerType != null) {
                commandSource.sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }

            else {
                commandSource.sendError(Text.translatable("commands.execute.conditional.fail"));
                return 0;
            }

        }

    }

    public static class GetNode {

        public static CommandNode<ServerCommandSource> get() {
            return literal("get")
                .then(argument("target", PowerHolderArgumentType.holder())
                    .then(argument("resource", PowerArgumentType.resource())
                        .suggests(PowerSuggestionProvider.resourcesFromEntity("target"))
                        .executes(GetNode::execute)
                        .then(literal("")))).build();
        }

        public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

            Entity target = PowerHolderArgumentType.getHolder(context, "target");
            Power power = PowerArgumentType.getResource(context, "resource");

            ServerCommandSource commandSource = context.getSource();
            PowerType powerType = power.getType(target);

            if (powerType != null) {

                int value = PowerUtil.getResourceValue(powerType);
                commandSource.sendFeedback(() -> Text.translatable("commands.scoreboard.players.get.success", target.getName(), value, power.getId().toString()), false);

                return value;

            }

            else {
                commandSource.sendError(Text.translatable("commands.scoreboard.players.get.null", power.getId().toString(), target.getName()));
                return 0;
            }

        }

    }

    public static class SetNode {

        public static CommandNode<ServerCommandSource> get() {
            return literal("set")
                .then(argument("target", PowerHolderArgumentType.holder())
                    .then(argument("resource", PowerArgumentType.resource())
                        .suggests(PowerSuggestionProvider.resourcesFromEntity("target"))
                        .then(argument("value", IntegerArgumentType.integer())
                            .executes(SetNode::execute)))).build();
        }

        public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

            Entity target = PowerHolderArgumentType.getHolder(context, "target");
            Power power = PowerArgumentType.getResource(context, "resource");

            int value = IntegerArgumentType.getInteger(context, "value");
            int newValue;

            ServerCommandSource commandSource = context.getSource();
            PowerType powerType = Optional
                .ofNullable(power.getType(target))
                .orElseThrow(() -> PowerArgumentType.POWER_NOT_GRANTED.create(target.getName(), power.getId().toString()));

            if (PowerUtil.setResourceValue(powerType, value)) {
                PowerHolderComponent.syncPower(target, power);
            }

            newValue = PowerUtil.getResourceValue(powerType);
            commandSource.sendFeedback(() -> Text.translatable("commands.scoreboard.players.set.success.single", power.getId().toString(), target.getName(), newValue), true);

            return newValue;

        }

    }

    public static class ChangeNode {

        public static CommandNode<ServerCommandSource> get() {
            return literal("change")
                .then(argument("target", PowerHolderArgumentType.holder())
                    .then(argument("resource", PowerArgumentType.resource())
                        .suggests(PowerSuggestionProvider.resourcesFromEntity("target"))
                        .then(argument("value", IntegerArgumentType.integer())
                            .executes(ChangeNode::execute)))).build();
        }

        public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

            Entity target = PowerHolderArgumentType.getHolder(context, "target");
            Power resource = PowerArgumentType.getResource(context, "resource");
            
            int value = IntegerArgumentType.getInteger(context, "value");
            int newValue;
            
            ServerCommandSource commandSource = context.getSource();
            PowerType powerType = Optional
                .ofNullable(resource.getType(target))
                .orElseThrow(() -> PowerArgumentType.POWER_NOT_GRANTED.create(target.getName(), resource.getId().toString()));
            
            if (PowerUtil.changeResourceValue(powerType, value)) {
                PowerHolderComponent.syncPower(target, resource);
            }
            
            newValue = PowerUtil.getResourceValue(powerType);
            commandSource.sendFeedback(() -> Text.translatable("commands.scoreboard.players.add.success.single", value, resource.getId().toString(), target.getName(), newValue), true);
            
            return newValue;

        }

    }

    public static class OperationNode {

        public static CommandNode<ServerCommandSource> get() {
            return literal("operation")
                .then(argument("target", PowerHolderArgumentType.holder())
                    .then(argument("resource", PowerArgumentType.resource())
                        .suggests(PowerSuggestionProvider.resourcesFromEntity("target"))
                        .then(argument("operation", PowerOperationArgumentType.operation())
                            .then(argument("source", ScoreHolderArgumentType.scoreHolder())
                                .then(argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                    .executes(OperationNode::execute)))))).build();
        }

        public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

            Entity target = PowerHolderArgumentType.getHolder(context, "target");
            Power resource = PowerArgumentType.getResource(context, "resource");

            PowerOperationArgumentType.Operation operation = PowerOperationArgumentType.getOperation(context, "operation");

            ScoreHolder source = ScoreHolderArgumentType.getScoreHolder(context, "source");
            ScoreboardObjective objective = ScoreboardObjectiveArgumentType.getObjective(context, "objective");

            ServerCommandSource commandSource = context.getSource();
            PowerType powerType = Optional
                .ofNullable(resource.getType(target))
                .orElseThrow(() -> PowerArgumentType.POWER_NOT_GRANTED.create(target.getName(), resource.getId().toString()));

            ScoreAccess scoreAccess = commandSource.getServer().getScoreboard().getOrCreateScore(source, objective);

            boolean operated = operation.apply(powerType, scoreAccess);
            int newValue = PowerUtil.getResourceValue(powerType);

            if (operated) {
                PowerHolderComponent.syncPower(target, resource);
            }

            commandSource.sendFeedback(() -> Text.translatable("commands.scoreboard.players.operation.success.single", resource.getId().toString(), target.getName(), newValue), true);
            return newValue;

        }

    }

}
