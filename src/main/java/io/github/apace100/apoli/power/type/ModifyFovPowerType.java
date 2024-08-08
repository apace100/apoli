package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

import java.util.List;

public class ModifyFovPowerType extends ValueModifyingPowerType {

    private final boolean affectedByFovEffectScale;

    public ModifyFovPowerType(Power power, LivingEntity entity, Modifier modifier, List<Modifier> modifiers, boolean affectedByFovEffectScale) {
        super(power, entity);
        this.affectedByFovEffectScale = affectedByFovEffectScale;

        if (modifier != null) {
            this.addModifier(modifier);
        }

        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }

    }

    public boolean isAffectedByFovEffectScale() {
        return affectedByFovEffectScale;
    }

    public static PowerTypeFactory<ModifyFovPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_fov"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null)
                .add("affected_by_fov_effect_scale", SerializableDataTypes.BOOLEAN, true),
            data -> (power, entity) -> new ModifyFovPowerType(power, entity,
                data.get("modifier"),
                data.get("modifiers"),
                data.get("affected_by_fov_effect_scale")
            )
        ).allowCondition();
    }

}
