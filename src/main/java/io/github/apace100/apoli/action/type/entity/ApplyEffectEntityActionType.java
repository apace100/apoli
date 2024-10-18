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
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.List;
import java.util.Optional;

public class ApplyEffectEntityActionType extends EntityActionType {

    public static final DataObjectFactory<ApplyEffectEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE.optional(), Optional.empty())
            .add("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES.optional(), Optional.empty()),
        data -> new ApplyEffectEntityActionType(
            data.get("effect"),
            data.get("effects")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("effect", actionType.effect)
            .set("effects", actionType.effects)
    );

    private final Optional<StatusEffectInstance> effect;
    private final Optional<List<StatusEffectInstance>> effects;

    private final List<StatusEffectInstance> allEffects;

    public ApplyEffectEntityActionType(Optional<StatusEffectInstance> effect, Optional<List<StatusEffectInstance>> effects) {

        this.effect = effect;
        this.effects = effects;

        this.allEffects = new ObjectArrayList<>();

        this.effect.ifPresent(this.allEffects::add);
        this.effects.ifPresent(this.allEffects::addAll);

    }

    @Override
    protected void execute(Entity entity) {

        if (!entity.getWorld().isClient() && entity instanceof LivingEntity living) {
            allEffects.forEach(living::addStatusEffect);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.APPLY_EFFECT;
    }

}
