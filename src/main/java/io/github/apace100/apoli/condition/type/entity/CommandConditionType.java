package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandConditionType {

    public static boolean condition(Entity entity, String command, Comparison comparison, int compareTo) {

        MinecraftServer server = entity.getServer();
        AtomicInteger result = new AtomicInteger();

        if (server == null) {
            return false;
        }

        ServerCommandSource source = entity.getCommandSource()
            .withOutput(Apoli.config.executeCommand.showOutput ? entity : CommandOutput.DUMMY)
            .withLevel(Apoli.config.executeCommand.permissionLevel)
            .withReturnValueConsumer((successful, returnValue) -> result.set(returnValue));

        server.getCommandManager().executeWithPrefix(source, command);
        return comparison.compare(result.get(), compareTo);

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("command"),
            new SerializableData()
                .add("command", SerializableDataTypes.STRING)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> condition(entity,
                data.get("command"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
