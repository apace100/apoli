package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class PreventEntitySelectionPower extends Power {

    private final Predicate<Pair<Entity, Entity>> biEntityCondition;

    public PreventEntitySelectionPower(PowerType type, LivingEntity entity, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        super(type, entity);
        this.biEntityCondition = biEntityCondition;
    }

    public boolean doesPrevent(Entity target) {
        return biEntityCondition == null
            || biEntityCondition.test(new Pair<>(entity, target));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("prevent_entity_selection"),
            new SerializableData()
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            data -> (powerType, livingEntity) -> new PreventEntitySelectionPower(
                powerType,
                livingEntity,
                data.get("bientity_condition")
            )
        ).allowCondition();
    }

}
