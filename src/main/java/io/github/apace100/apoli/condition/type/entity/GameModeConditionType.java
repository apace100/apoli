package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.ClientPlayerEntityAccessor;
import io.github.apace100.apoli.mixin.ClientPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.mixin.ServerPlayerInteractionManagerAccessor;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class GameModeConditionType {

    public static boolean condition(Entity entity, GameMode gameMode) {

        if (!(entity instanceof PlayerEntity player)) {
            return false;
        }

        else if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayerInteractionManagerAccessor interactionManager = (ServerPlayerInteractionManagerAccessor) serverPlayer.interactionManager;
            return interactionManager.getGameMode() == gameMode;
        }

        else if (player instanceof ClientPlayerEntity clientPlayer) {
            ClientPlayerInteractionManagerAccessor interactionManager = (ClientPlayerInteractionManagerAccessor) (((ClientPlayerEntityAccessor) clientPlayer).getClient()).interactionManager;
            return interactionManager != null && interactionManager.getGameMode() == gameMode;
        }

        else {
            return false;
        }

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("gamemode"),
            new SerializableData()
                .add("gamemode", ApoliDataTypes.GAME_MODE),
            (data, entity) -> condition(entity,
                data.get("gamemode")
            )
        );
    }

}
