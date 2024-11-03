package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PreventEntityRenderPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventEntityRenderPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new PreventEntityRenderPowerType(
            data.get("entity_condition"),
            data.get("bientity_condition"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_condition", powerType.entityCondition)
            .set("bientity_condition", powerType.biEntityCondition)
    );

    private final Optional<EntityCondition> entityCondition;
    private final Optional<BiEntityCondition> biEntityCondition;

    public PreventEntityRenderPowerType(Optional<EntityCondition> entityCondition, Optional<BiEntityCondition> biEntityCondition, Optional<EntityCondition> condition) {
        super(condition);
        this.entityCondition = entityCondition;
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_ENTITY_RENDER;
    }

    public boolean doesApply(Entity entity) {
        return entityCondition.map(condition -> condition.test(entity)).orElse(true)
            && biEntityCondition.map(condition -> condition.test(getHolder(), entity)).orElse(true);
    }

}
