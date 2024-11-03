package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PreventEntitySelectionPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventEntitySelectionPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new PreventEntitySelectionPowerType(
            data.get("bientity_condition"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("bientity_condition", powerType.biEntityCondition)
    );

    private final Optional<BiEntityCondition> biEntityCondition;

    public PreventEntitySelectionPowerType(Optional<BiEntityCondition> biEntityCondition, Optional<EntityCondition> condition) {
        super(condition);
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_ENTITY_SELECTION;
    }

    public boolean doesPrevent(Entity target) {
        return biEntityCondition
            .map(condition -> condition.test(getHolder(), target))
            .orElse(true);
    }

}
