package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class ExecuteCommandBlockActionType extends BlockActionType {

    public static final DataObjectFactory<ExecuteCommandBlockActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
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

        MinecraftServer server = world.getServer();
        if (server == null) {
            return;
        }

        ServerCommandSource commandSource = server.getCommandSource()
            .withOutput(Apoli.config.executeCommand.showOutput ? server : CommandOutput.DUMMY)
            .withPosition(pos.toCenterPos())
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        server.getCommandManager().executeWithPrefix(commandSource, command);

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BlockActionTypes.EXECUTE_COMMAND;
    }

}
