package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

import java.util.List;

public class ModifyFovPower extends ValueModifyingPower {

    private final boolean affectedByFovEffectScale;

    public ModifyFovPower(PowerType<?> type, LivingEntity entity, Modifier modifier, List<Modifier> modifiers, boolean affectedByFovEffectScale) {
        super(type, entity);
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

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("modify_fov"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null)
                .add("affected_by_fov_effect_scale", SerializableDataTypes.BOOLEAN, true),
            data -> (powerType, entity) -> new ModifyFovPower(
                powerType,
                entity,
                data.get("modifier"),
                data.get("modifiers"),
                data.get("affected_by_fov_effect_scale")
            )
        ).allowCondition();
    }

}
