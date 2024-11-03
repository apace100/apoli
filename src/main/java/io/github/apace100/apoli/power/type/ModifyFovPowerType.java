package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifyFovPowerType extends ValueModifyingPowerType {

    public static final TypedDataObjectFactory<ModifyFovPowerType> DATA_FACTORY = createConditionedModifyingDataFactory(
        new SerializableData()
            .add("affected_by_fov_effect_scale", SerializableDataTypes.BOOLEAN, true),
        (data, modifiers, condition) -> new ModifyFovPowerType(
            data.get("affected_by_fov_effect_scale"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("affected_by_fov_effect_scale", powerType.affectedByFovEffectScale)
    );

    private final boolean affectedByFovEffectScale;

    public ModifyFovPowerType(boolean affectedByFovEffectScale, List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);
        this.affectedByFovEffectScale = affectedByFovEffectScale;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_FOV;
    }

    public boolean isAffectedByFovEffectScale() {
        return affectedByFovEffectScale;
    }

}
