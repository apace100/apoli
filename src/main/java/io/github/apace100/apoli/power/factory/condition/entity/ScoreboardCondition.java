package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;

public class ScoreboardCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        ScoreHolder scoreHolder = ScoreHolder.fromName(entity.getNameForScoreboard());
        Scoreboard scoreboard = entity.getWorld().getScoreboard();

        ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(data.get("objective"));
        if (scoreboardObjective == null) {
            return false;
        }

        Comparison comparison = data.get("comparison");
        int compareTo = data.get("compare_to");

        ScoreAccess scoreAccess = scoreboard.getOrCreateScore(scoreHolder, scoreboardObjective);
        return comparison.compare(scoreAccess.getScore(), compareTo);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("scoreboard"),
            new SerializableData()
                .add("name", SerializableDataTypes.STRING, null)
                .add("objective", SerializableDataTypes.STRING)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            ScoreboardCondition::condition
        );
    }
}
