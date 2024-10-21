package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<CommandEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("command", SerializableDataTypes.STRING)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new CommandEntityConditionType(
            data.get("command"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("command", conditionType.command)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final String command;

    private final Comparison comparison;
    private final int compareTo;

    public CommandEntityConditionType(String command, Comparison comparison, int compareTo) {
        this.command = command;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }

        MinecraftServer server = serverWorld.getServer();
        AtomicInteger result = new AtomicInteger();

        ServerCommandSource commandSource = entity.getCommandSource()
            .withReturnValueConsumer((successful, returnValue) -> result.set(returnValue))
            .withLevel(Apoli.config.executeCommand.permissionLevel)
            .withOutput(CommandOutput.DUMMY);

        if (Apoli.config.executeCommand.showOutput) {

            CommandOutput output = entity instanceof ServerPlayerEntity serverPlayer && serverPlayer.networkHandler != null
                ? serverPlayer
                : server;

            commandSource = commandSource.withOutput(output);

        }

        server.getCommandManager().executeWithPrefix(commandSource, command);
        return comparison.compare(result.get(), compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.COMMAND;
    }

}
