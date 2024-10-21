package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.mixin.ClientPlayerEntityAccessor;
import io.github.apace100.apoli.mixin.ClientPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.mixin.ServerPlayerInteractionManagerAccessor;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class GameModeEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<GameModeEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("gamemode", ApoliDataTypes.GAME_MODE),
        data -> new GameModeEntityConditionType(
            data.get("gamemode")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("gamemode", conditionType.gameMode)
    );

    private final GameMode gameMode;

    public GameModeEntityConditionType(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public boolean test(Entity entity) {

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

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.GAME_MODE;
    }

}
