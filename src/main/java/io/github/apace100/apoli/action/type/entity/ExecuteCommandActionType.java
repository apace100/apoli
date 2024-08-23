package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;

public class ExecuteCommandActionType {

    public static void action(Entity entity, String command) {

        MinecraftServer server = entity.getServer();
        if (server == null) {
            return;
        }

        ServerCommandSource source = entity.getCommandSource()
            .withOutput(Apoli.config.executeCommand.showOutput ? entity : CommandOutput.DUMMY)
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        server.getCommandManager().executeWithPrefix(source, command);

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("execute_command"),
            new SerializableData()
                .add("command", SerializableDataTypes.STRING),
            (data, entity) -> action(entity,
                data.get("command")
            )
        );
    }

}
