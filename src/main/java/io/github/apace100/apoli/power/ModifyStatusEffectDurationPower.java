package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;

import java.util.LinkedList;
import java.util.List;

public class ModifyStatusEffectDurationPower extends ValueModifyingPower {
    private final List<StatusEffect> statusEffects;

    public ModifyStatusEffectDurationPower(PowerType<?> type, LivingEntity entity, List<StatusEffect> statusEffects) {
        super(type, entity);
        this.statusEffects = statusEffects;
    }

    public boolean doesApply(StatusEffect statusEffect) {
        return statusEffects == null || statusEffects.contains(statusEffect);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<ModifyStatusEffectDurationPower>(
            Apoli.identifier("modify_status_effect_duration"),
            new SerializableData()
                .add("status_effect", SerializableDataTypes.STATUS_EFFECT, null)
                .add("status_effects", SerializableDataTypes.STATUS_EFFECTS, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (type, player) -> {
                List<StatusEffect> statusEffects = new LinkedList<>();
                data.<StatusEffect>ifPresent("status_effect", statusEffects::add);
                data.<List<StatusEffect>>ifPresent("status_effects", statusEffects::addAll);
                ModifyStatusEffectDurationPower power = new ModifyStatusEffectDurationPower(type, player,
                    data.isPresent("status_effect") || data.isPresent("status_effect") ? statusEffects : null);
                data.ifPresent("modifier", power::addModifier);
                data.<List<Modifier>>ifPresent("modifiers", l -> l.forEach(power::addModifier));

                return power;
            })
            .allowCondition();
    }
}
