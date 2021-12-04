package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;

public class ModifyStatusEffectDurationPower extends ValueModifyingPower {
    private final StatusEffect statusEffect;

    public ModifyStatusEffectDurationPower(PowerType<?> type, LivingEntity entity, StatusEffect statusEffect) {
        super(type, entity);
        this.statusEffect = statusEffect;
    }

    public boolean doesApply(StatusEffect statusEffect) {
        return statusEffect.equals(this.statusEffect);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<ModifyStatusEffectDurationPower>(
                Apoli.identifier("modify_status_effect_duration"),
                new SerializableData()
                        .add("status_effect", SerializableDataTypes.STATUS_EFFECT)
                        .add("modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER),
                data -> (type, player) -> {
                    ModifyStatusEffectDurationPower modifyStatusEffectAmplifierPower = new ModifyStatusEffectDurationPower(type, player, (StatusEffect) data.get("status_effect"));
                    modifyStatusEffectAmplifierPower.addModifier((EntityAttributeModifier) data.get("modifier"));
                    return modifyStatusEffectAmplifierPower;
                })
                .allowCondition();
    }
}
