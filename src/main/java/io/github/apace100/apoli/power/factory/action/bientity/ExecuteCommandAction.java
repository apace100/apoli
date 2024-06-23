package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ExecuteCommandAction {
	public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {
		Entity actor = actorAndTarget.getLeft();

		UUID actorUUID = actor.getUuid();
		UUID targetUUID = actorAndTarget.getRight().getUuid();

		String actorSelector = data.getString("actor_selector");
		String targetSelector = data.getString("target_selector");

		MinecraftServer server = actor.getWorld().getServer();
		if (server != null) {
			ServerCommandSource source = getCommandSource(actor, server);
			String command = data.getString("command").replace(actorSelector, actorUUID.toString())
					.replace(targetSelector, targetUUID.toString());
			server.getCommandManager().executeWithPrefix(source, command);
		}
	}

	// IntelliJ practically forced me to do this :skull:
	@NotNull
	private static ServerCommandSource getCommandSource(Entity actor, MinecraftServer server) {
		boolean validOutput = !(actor instanceof ServerPlayerEntity) || ((ServerPlayerEntity) actor).networkHandler != null;
		return new ServerCommandSource(
				Apoli.config.executeCommand.showOutput && validOutput ? actor : CommandOutput.DUMMY,
				actor.getPos(),
				actor.getRotationClient(),
				actor.getWorld() instanceof ServerWorld ? (ServerWorld) actor.getWorld() : null,
				Apoli.config.executeCommand.permissionLevel,
				actor.getName().getString(),
				actor.getDisplayName(),
				server,
				actor);
	}

	public static ActionFactory<Pair<Entity, Entity>> getFactory() {
		return new ActionFactory<>(Apoli.identifier("execute_command"),
				new SerializableData()
						.add("command", SerializableDataTypes.STRING)
						.add("actor_selector", SerializableDataTypes.STRING, "%a")
						.add("target_selector", SerializableDataTypes.STRING, "%t"),
				ExecuteCommandAction::action
		);
	}
}
