package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class DimensionConditionType {

    public static boolean condition(Entity entity, RegistryKey<World> dimensionKey) {
        return entity.getWorld().getRegistryKey().equals(dimensionKey);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("dimension"),
            new SerializableData()
                .add("dimension", SerializableDataTypes.DIMENSION),
            (data, entity) -> condition(entity,
                data.get("dimension")
            )
        );
    }

}
