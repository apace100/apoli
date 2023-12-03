package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.ClientPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.mixin.ServerPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class GameModeCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        if (!(entity instanceof PlayerEntity playerEntity)) {
            return false;
        }

        GameMode specifiedGameMode = data.get("gamemode");
        if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {

            ServerPlayerInteractionManagerAccessor interactionManager = (ServerPlayerInteractionManagerAccessor) serverPlayerEntity.interactionManager;
            return interactionManager.getGameMode() == specifiedGameMode;

        } else if (playerEntity instanceof ClientPlayerEntity) {

            ClientPlayerInteractionManagerAccessor interactionManager = (ClientPlayerInteractionManagerAccessor) MinecraftClient.getInstance().interactionManager;
            return interactionManager != null && interactionManager.getGameMode() == specifiedGameMode;

        }

        return false;

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("gamemode"),
            new SerializableData()
                .add("gamemode", ApoliDataTypes.GAME_MODE),
            GameModeCondition::condition
        );
    }

}
