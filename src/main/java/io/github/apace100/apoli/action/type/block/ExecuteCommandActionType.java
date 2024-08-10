package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class ExecuteCommandActionType {

    public static void action(World world, BlockPos pos, String command) {

        MinecraftServer server = world.getServer();
        if (server == null) {
            return;
        }

        ServerCommandSource source = server.getCommandSource()
            .withOutput(Apoli.config.executeCommand.showOutput ? server : CommandOutput.DUMMY)
            .withPosition(pos.toCenterPos())
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        server.getCommandManager().executeWithPrefix(source, command);

    }

    public static ActionTypeFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("execute_command"),
            new SerializableData()
                .add("command", SerializableDataTypes.STRING),
            (data, block) -> action(block.getLeft(), block.getMiddle(),
                data.get("command")
            )
        );
    }

}
