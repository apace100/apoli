package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class PreventEntitySelectionPowerType extends PowerType {

    private final Predicate<Pair<Entity, Entity>> biEntityCondition;

    public PreventEntitySelectionPowerType(Power power, LivingEntity entity, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        super(power, entity);
        this.biEntityCondition = biEntityCondition;
    }

    public boolean doesPrevent(Entity target) {
        return biEntityCondition == null
            || biEntityCondition.test(new Pair<>(entity, target));
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_entity_selection"),
            new SerializableData()
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            data -> (power, entity) -> new PreventEntitySelectionPowerType(power, entity,
                data.get("bientity_condition")
            )
        ).allowCondition();
    }

}
