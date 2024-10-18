package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;
import java.util.Optional;

public class ClearEffectEntityActionType extends EntityActionType {

    public static final DataObjectFactory<ClearEffectEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT_ENTRY.optional(), Optional.empty())
            .add("effects", SerializableDataTypes.STATUS_EFFECT_ENTRIES.optional(), Optional.empty()),
        data -> new ClearEffectEntityActionType(
            data.get("effect"),
            data.get("effects")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("effect", actionType.effect)
            .set("effects", actionType.effects)
    );

    private final Optional<RegistryEntry<StatusEffect>> effect;
    private final Optional<List<RegistryEntry<StatusEffect>>> effects;

    private final List<RegistryEntry<StatusEffect>> allEffects;

    public ClearEffectEntityActionType(Optional<RegistryEntry<StatusEffect>> effect, Optional<List<RegistryEntry<StatusEffect>>> effects) {

        this.effect = effect;
        this.effects = effects;

        this.allEffects = new ObjectArrayList<>();

        this.effect.ifPresent(this.allEffects::add);
        this.effects.ifPresent(this.allEffects::addAll);

    }

    @Override
    protected void execute(Entity entity) {

        if (entity instanceof LivingEntity living) {

            if (!allEffects.isEmpty()) {
                allEffects.forEach(living::removeStatusEffect);
            }

            else {
                living.clearStatusEffects();
            }

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.CLEAR_EFFECT;
    }

}
