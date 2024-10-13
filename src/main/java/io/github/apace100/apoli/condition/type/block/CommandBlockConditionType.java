package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandBlockConditionType extends BlockConditionType {

    public static final DataObjectFactory<CommandBlockConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("command", SerializableDataTypes.STRING)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN)
            .add("compare_to", SerializableDataTypes.INT, 0),
        data -> new CommandBlockConditionType(
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

    public CommandBlockConditionType(String command, Comparison comparison, int compareTo) {
        this.command = command;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(World world, BlockPos pos) {

        MinecraftServer server = world.getServer();
        if (server == null) {
            return false;
        }

        AtomicInteger result = new AtomicInteger();
        ServerCommandSource source = server.getCommandSource()
            .withOutput(Apoli.config.executeCommand.showOutput ? server : CommandOutput.DUMMY)
            .withPosition(pos.toCenterPos())
            .withLevel(Apoli.config.executeCommand.permissionLevel)
            .withReturnValueConsumer((successful, returnValue) -> result.set(returnValue));

        server.getCommandManager().executeWithPrefix(source, command);
        return comparison.compare(result.get(), compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.COMMAND;
    }

}
