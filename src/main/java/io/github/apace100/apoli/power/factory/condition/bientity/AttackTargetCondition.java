package io.github.apace100.apoli.power.factory.condition.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Pair;

public class AttackTargetCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        Entity actor = actorAndTarget.getLeft();
        Entity target = actorAndTarget.getRight();

        if (actor == null || target == null) {
            return false;
        }

        return (actor instanceof MobEntity mobActor && target.equals(mobActor.getTarget()))
            || (actor instanceof Angerable angerableActor && target.equals(angerableActor.getTarget()));

    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("attack_target"),
            new SerializableData(),
            AttackTargetCondition::condition
        );
    }

}
