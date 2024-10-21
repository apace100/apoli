package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandBlockConditionType extends BlockConditionType {

    public static final TypedDataObjectFactory<CommandBlockConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
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

        if (!(world instanceof ServerWorld serverWorld)) {
            return false;
        }

        MinecraftServer server = serverWorld.getServer();
        AtomicInteger result = new AtomicInteger();

        BlockState blockState = world.getBlockState(pos);
        String blockTranslationKey = blockState.getBlock().getTranslationKey();

        ServerCommandSource commandSource = new ServerCommandSource(
            Apoli.config.executeCommand.showOutput ? server : CommandOutput.DUMMY,
            pos.toCenterPos(),
            Vec2f.ZERO,
            serverWorld,
            Apoli.config.executeCommand.permissionLevel,
            blockTranslationKey,
            Text.translatable(blockTranslationKey),
            server,
            null
        );

        commandSource = commandSource.withReturnValueConsumer((successful, returnValue) -> result.set(returnValue));
        server.getCommandManager().executeWithPrefix(commandSource, command);

        return comparison.compare(result.get(), compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.COMMAND;
    }

}
