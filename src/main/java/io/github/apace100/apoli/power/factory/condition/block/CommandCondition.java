package io.github.apace100.apoli.power.factory.condition.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

public class CommandCondition {

    public static boolean condition(SerializableData.Instance data, CachedBlockPosition cachedBlockPosition) {

        MinecraftServer server = ((World) cachedBlockPosition.getWorld()).getServer();
        if (server == null) {
            return false;
        }

        ServerCommandSource source = server.getCommandSource()
            .withPosition(cachedBlockPosition.getBlockPos().toCenterPos())
            .withLevel(Apoli.config.executeCommand.permissionLevel)
            .withSilent();
        Comparison comparison = data.get("comparison");
        String command = data.get("command");

        int compareTo = data.get("compare_to");
        int result = server.getCommandManager().executeWithPrefix(source, command);

        return comparison.compare(result, compareTo);

    }

    public static ConditionFactory<CachedBlockPosition> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("command"),
            new SerializableData()
                .add("command", SerializableDataTypes.STRING)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
                .add("compare_to", SerializableDataTypes.INT, 1),
            CommandCondition::condition
        );
    }

}
