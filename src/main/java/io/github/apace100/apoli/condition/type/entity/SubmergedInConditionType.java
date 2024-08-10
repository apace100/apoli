package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

public class SubmergedInConditionType {

    public static boolean condition(Entity entity, TagKey<Fluid> fluidTag) {
        return ((SubmergableEntity) entity).apoli$isSubmergedInLoosely(fluidTag);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("submerged_in"),
            new SerializableData()
                .add("fluid", SerializableDataTypes.FLUID_TAG),
            (data, entity) -> condition(entity,
                data.get("fluid")
            )
        );
    }

}
