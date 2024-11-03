package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class EffectImmunityPowerType extends PowerType {

    public static final TypedDataObjectFactory<EffectImmunityPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT_ENTRY, null)
            .addFunctionedDefault("effects", SerializableDataTypes.STATUS_EFFECT_ENTRIES, data -> MiscUtil.singletonListOrEmpty(data.get("effect")))
            .add("inverted", SerializableDataTypes.BOOLEAN, false),
        (data, condition) -> new EffectImmunityPowerType(
            data.get("effects"),
            data.get("inverted"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("effects", powerType.effects)
            .set("inverted", powerType.inverted)
    );

    protected final List<RegistryEntry<StatusEffect>> effects;
    protected final boolean inverted;

    public EffectImmunityPowerType(List<RegistryEntry<StatusEffect>> effects, boolean inverted, Optional<EntityCondition> condition) {
        super(condition);
        this.effects = effects;
        this.inverted = inverted;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.EFFECT_IMMUNITY;
    }

    public boolean doesApply(StatusEffectInstance instance) {
        return doesApply(instance.getEffectType());
    }

    public boolean doesApply(RegistryEntry<StatusEffect> effect) {
        return inverted ^ effects.contains(effect);
    }

}
