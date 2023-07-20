package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.HashSet;
import java.util.List;

public class EffectImmunityPower extends Power {

    protected final HashSet<StatusEffect> effects = new HashSet<>();
    private final boolean inverted;

    public EffectImmunityPower(PowerType<?> type, LivingEntity entity, boolean inverted) {
        super(type, entity);
        this.inverted = inverted;
    }

    public EffectImmunityPower addEffect(StatusEffect effect) {
        effects.add(effect);
        return this;
    }

    public boolean doesApply(StatusEffectInstance instance) {
        return doesApply(instance.getEffectType());
    }

    public boolean doesApply(StatusEffect effect) {
        return inverted ^ effects.contains(effect);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("effect_immunity"),
            new SerializableData()
                .add("effect", SerializableDataTypes.STATUS_EFFECT, null)
                .add("effects", SerializableDataTypes.STATUS_EFFECTS, null)
                .add("inverted", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) -> {
                    EffectImmunityPower power = new EffectImmunityPower(type, player, data.get("inverted"));
                    if(data.isPresent("effect")) {
                        power.addEffect(data.get("effect"));
                    }
                    if(data.isPresent("effects")) {
                        ((List<StatusEffect>)data.get("effects")).forEach(power::addEffect);
                    }
                    return power;
                })
            .allowCondition();
    }
}
