package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public class DimensionEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<DimensionEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("dimension", SerializableDataTypes.DIMENSION),
        data -> new DimensionEntityConditionType(
            data.get("dimension")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("dimension", conditionType.dimension)
    );

    private final RegistryKey<World> dimension;

    public DimensionEntityConditionType(RegistryKey<World> dimension) {
        this.dimension = dimension;
    }

    @Override
    public boolean test(Entity entity) {
        return entity.getWorld().getRegistryKey().equals(dimension);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.DIMENSION;
    }

}
