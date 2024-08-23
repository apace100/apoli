package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

public class FluidHeightConditionType {

    public static boolean condition(Entity entity, TagKey<Fluid> fluidTag, Comparison comparison, double compareTo) {
        return comparison.compare(((SubmergableEntity) entity).apoli$getFluidHeightLoosely(fluidTag), compareTo);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("fluid_height"),
            new SerializableData()
                .add("fluid", SerializableDataTypes.FLUID_TAG)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, entity) -> condition(entity,
                data.get("fluid"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
