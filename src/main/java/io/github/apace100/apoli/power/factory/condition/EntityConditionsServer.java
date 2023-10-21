package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.ServerPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;

public final class EntityConditionsServer {

    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("using_effective_tool"), new SerializableData(),
            (data, entity) -> {
                if(entity instanceof ServerPlayerEntity) {
                    ServerPlayerInteractionManagerAccessor interactionMngr = ((ServerPlayerInteractionManagerAccessor)((ServerPlayerEntity)entity).interactionManager);
                    if(interactionMngr.getMining()) {
                        return ((PlayerEntity)entity).canHarvest(entity.getWorld().getBlockState(interactionMngr.getMiningPos()));
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("gamemode"), new SerializableData()
            .add("gamemode", SerializableDataTypes.STRING), (data, entity) -> {
            if(entity instanceof ServerPlayerEntity) {
                ServerPlayerInteractionManagerAccessor interactionMngr = ((ServerPlayerInteractionManagerAccessor)((ServerPlayerEntity)entity).interactionManager);
                return interactionMngr.getGameMode().getName().equals(data.getString("gamemode"));
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("glowing"), new SerializableData(),
            (data, entity) -> entity.isGlowing()));
    }

    private static void register(ConditionFactory<Entity> conditionFactory) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
