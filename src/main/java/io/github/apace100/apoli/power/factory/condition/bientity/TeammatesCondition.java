package io.github.apace100.apoli.power.factory.condition.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Pair;

public class TeammatesCondition {

    public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {
        Entity actor = actorAndTarget.getLeft();
        Entity target = actorAndTarget.getRight();

        if (actor == null || target == null) {
            return false;
        }

        Team actorTeam = actor.getScoreboardTeam();
        Team targetTeam = target.getScoreboardTeam();

        boolean allowEmpty = data.getBoolean("allow_empty");

        if (actorTeam == null || targetTeam == null) {
            return allowEmpty && (actorTeam == null && targetTeam == null);
        }

        return actorTeam.isEqual(targetTeam);
    }

    public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("teammates"),
            new SerializableData()
                .add("allow_empty", SerializableDataTypes.BOOLEAN, false),
            TeammatesCondition::condition
        );
    }

}
