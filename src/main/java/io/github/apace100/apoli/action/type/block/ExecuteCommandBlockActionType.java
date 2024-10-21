package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

import java.util.Optional;

public class ExecuteCommandBlockActionType extends BlockActionType {

    public static final TypedDataObjectFactory<ExecuteCommandBlockActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("command", SerializableDataTypes.STRING),
        data -> new ExecuteCommandBlockActionType(
            data.get("command")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("command", actionType.command)
    );

    private final String command;

    public ExecuteCommandBlockActionType(String command) {
        this.command = command;
    }

    @Override
	protected void execute(World world, BlockPos pos, Optional<Direction> direction) {

        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        BlockState blockState = world.getBlockState(pos);
        String blockTranslationKey = blockState.getBlock().getTranslationKey();

        MinecraftServer server = serverWorld.getServer();
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

        server.getCommandManager().executeWithPrefix(commandSource, command);

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BlockActionTypes.EXECUTE_COMMAND;
    }

}
