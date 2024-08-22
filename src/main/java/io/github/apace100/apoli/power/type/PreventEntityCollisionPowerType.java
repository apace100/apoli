package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class PreventEntityCollisionPowerType extends PowerType {

    private final Predicate<Pair<Entity, Entity>> biEntityCondition;

    public PreventEntityCollisionPowerType(Power power, LivingEntity entity, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        super(power, entity);
        this.biEntityCondition = biEntityCondition;
    }

    public boolean doesApply(Entity target) {
        return biEntityCondition == null || biEntityCondition.test(new Pair<>(entity, target));
    }

    public static boolean doesApply(Entity fromEntity, Entity collidingEntity) {
        return PowerHolderComponent.hasPowerType(fromEntity, PreventEntityCollisionPowerType.class, p -> p.doesApply(collidingEntity))
            || PowerHolderComponent.hasPowerType(collidingEntity, PreventEntityCollisionPowerType.class, p -> p.doesApply(fromEntity));
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_entity_collision"),
            new SerializableData()
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            data -> (power, entity) -> new PreventEntityCollisionPowerType(power, entity,
                data.get("bientity_condition")
            )
        ).allowCondition();
    }

}
