package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        MinecraftServer server = entity.getServer();
        AtomicInteger result = new AtomicInteger();

        if (server == null) {
            return false;
        }

        CommandOutput commandOutput = Apoli.config.executeCommand.showOutput && (!(entity instanceof ServerPlayerEntity player) || player.networkHandler != null)
            ? entity
            : CommandOutput.DUMMY;
        ServerCommandSource source = entity.getCommandSource()
            .withOutput(commandOutput)
            .withLevel(Apoli.config.executeCommand.permissionLevel)
            .withReturnValueConsumer((successful, returnValue) -> result.set(returnValue));

        Comparison comparison = data.get("comparison");
        String command = data.get("command");

        int compareTo = data.get("compare_to");
        server.getCommandManager().executeWithPrefix(source, command);

        return comparison.compare(result.get(), compareTo);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("command"),
            new SerializableData()
                .add("command", SerializableDataTypes.STRING)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            CommandCondition::condition
        );
    }

}
