package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ExecuteCommandEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ExecuteCommandEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("command", SerializableDataTypes.STRING),
        data -> new ExecuteCommandEntityActionType(
            data.get("command")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("command", actionType.command)
    );

    private final String command;

    public ExecuteCommandEntityActionType(String command) {
        this.command = command;
    }

    @Override
    protected void execute(Entity entity) {

        MinecraftServer server = entity.getServer();
        if (server == null) {
            return;
        }

        ServerCommandSource commandSource = entity.getCommandSource()
            .withOutput(CommandOutput.DUMMY)
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        if (Apoli.config.executeCommand.showOutput) {
            commandSource = commandSource.withOutput(entity instanceof ServerPlayerEntity serverPlayer && serverPlayer.networkHandler != null
                ? serverPlayer
                : server);
        }

        server.getCommandManager().executeWithPrefix(commandSource, command);

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.EXECUTE_COMMAND;
    }

}
