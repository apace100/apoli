package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModifyStatusEffectDurationPowerType extends ValueModifyingPowerType {

    public static final TypedDataObjectFactory<ModifyStatusEffectDurationPowerType> DATA_FACTORY = createConditionedModifyingDataFactory(
        new SerializableData()
            .add("status_effect", SerializableDataTypes.STATUS_EFFECT_ENTRY, null)
            .addFunctionedDefault("status_effects", SerializableDataTypes.STATUS_EFFECT_ENTRIES, data -> MiscUtil.singletonListOrEmpty(data.get("status_effect"))),
        (data, modifiers, condition) -> new ModifyStatusEffectDurationPowerType(
            data.get("status_effects"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("status_effects", powerType.statusEffects)
    );

    private final List<RegistryEntry<StatusEffect>> statusEffects;

    public ModifyStatusEffectDurationPowerType(List<RegistryEntry<StatusEffect>> statusEffects, List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);
        this.statusEffects = statusEffects
            .stream()
            .distinct()
            .collect(Collectors.toCollection(ObjectArrayList::new));
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_STATUS_EFFECT_DURATION;
    }

    public boolean doesApply(RegistryEntry<StatusEffect> statusEffect) {
        return statusEffects.isEmpty()
            || statusEffects.contains(statusEffect);
    }

}
