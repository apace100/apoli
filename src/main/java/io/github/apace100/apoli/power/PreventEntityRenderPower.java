package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Predicate;

public class PreventEntityRenderPower extends Power {

    private final Predicate<Entity> entityCondition;

    public PreventEntityRenderPower(PowerType<?> type, LivingEntity entity, Predicate<Entity> entityCondition) {
        super(type, entity);
        this.entityCondition = entityCondition;
    }

    public boolean doesApply(Entity e) {
        return (entityCondition == null || entityCondition.test(e));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_entity_render"),
            new SerializableData()
                .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            data ->
                (type, player) -> new PreventEntityRenderPower(type, player, (ConditionFactory<Entity>.Instance)data.get("entity_condition")))
            .allowCondition();
    }
}
