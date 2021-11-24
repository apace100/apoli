package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class PreventEntityCollisionPower extends Power {

    private final Predicate<Pair<Entity, Entity>> bientityCondition;

    public PreventEntityCollisionPower(PowerType<?> type, LivingEntity entity, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity);
        this.bientityCondition = bientityCondition;
    }

    public boolean doesApply(Entity e) {
        return bientityCondition == null || bientityCondition.test(new Pair<>(entity, e));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_entity_collision"),
            new SerializableData()
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            data ->
                (type, player) -> new PreventEntityCollisionPower(type, player,
                    (ConditionFactory<Pair<Entity, Entity>>.Instance)data.get("bientity_condition")))
            .allowCondition();
    }
}
