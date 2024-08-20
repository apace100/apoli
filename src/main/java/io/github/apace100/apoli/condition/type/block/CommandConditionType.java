package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock, String command, Comparison comparison, int compareTo) {

        MinecraftServer server = ((World) cachedBlock.getWorld()).getServer();
        if (server == null) {
            return false;
        }

        AtomicInteger result = new AtomicInteger();
        ServerCommandSource source = server.getCommandSource()
            .withOutput(Apoli.config.executeCommand.showOutput ? server : CommandOutput.DUMMY)
            .withPosition(cachedBlock.getBlockPos().toCenterPos())
            .withLevel(Apoli.config.executeCommand.permissionLevel)
            .withReturnValueConsumer((successful, returnValue) -> result.set(returnValue));

        server.getCommandManager().executeWithPrefix(source, command);
        return comparison.compare(result.get(), compareTo);

    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("command"),
            new SerializableData()
                .add("command", SerializableDataTypes.STRING)
                .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
                .add("compare_to", SerializableDataTypes.INT, 1),
            (data, cachedBlock) -> condition(cachedBlock,
                data.get("command"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
