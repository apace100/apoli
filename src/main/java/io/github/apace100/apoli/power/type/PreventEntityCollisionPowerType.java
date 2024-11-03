package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PreventEntityCollisionPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventEntityCollisionPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new PreventEntityCollisionPowerType(
            data.get("bientity_condition"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("bientity_condition", powerType.biEntityCondition)
    );

    private final Optional<BiEntityCondition> biEntityCondition;

    public PreventEntityCollisionPowerType(Optional<BiEntityCondition> biEntityCondition, Optional<EntityCondition> condition) {
        super(condition);
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_ENTITY_COLLISION;
    }

    public boolean doesApply(Entity target) {
        return biEntityCondition
            .map(condition -> condition.test(getHolder(), target))
            .orElse(true);
    }

    public static boolean doesApply(Entity fromEntity, Entity collidingEntity) {
        return PowerHolderComponent.hasPowerType(fromEntity, PreventEntityCollisionPowerType.class, p -> p.doesApply(collidingEntity))
            || PowerHolderComponent.hasPowerType(collidingEntity, PreventEntityCollisionPowerType.class, p -> p.doesApply(fromEntity));
    }

}
