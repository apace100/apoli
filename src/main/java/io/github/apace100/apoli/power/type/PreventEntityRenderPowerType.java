package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class PreventEntityRenderPowerType extends PowerType {

    private final Predicate<Entity> entityCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    public PreventEntityRenderPowerType(Power power, LivingEntity entity, Predicate<Entity> entityCondition, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(power, entity);
        this.entityCondition = entityCondition;
        this.bientityCondition = bientityCondition;
    }

    public boolean doesApply(Entity e) {
        return (entityCondition == null || entityCondition.test(e))
            && (bientityCondition == null || bientityCondition.test(new Pair<>(entity, e)));
    }

    public static PowerTypeFactory<PreventEntityRenderPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_entity_render"),
            new SerializableData()
                .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            data -> (power, entity) -> new PreventEntityRenderPowerType(power, entity,
                data.get("entity_condition"),
                data.get("bientity_condition")
            )
        ).allowCondition();
    }

}
