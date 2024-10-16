package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

public class SubmergedInEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<SubmergedInEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("fluid", SerializableDataTypes.FLUID_TAG),
        data -> new SubmergedInEntityConditionType(
            data.get("fluid")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("fluid", conditionType.fluid)
    );

    private final TagKey<Fluid> fluid;

    public SubmergedInEntityConditionType(TagKey<Fluid> fluid) {
        this.fluid = fluid;
    }

    @Override
    public boolean test(Entity entity) {
        return entity instanceof SubmergableEntity submergableEntity
            && submergableEntity.apoli$isSubmergedInLoosely(fluid);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.SUBMERGED_IN;
    }

}
