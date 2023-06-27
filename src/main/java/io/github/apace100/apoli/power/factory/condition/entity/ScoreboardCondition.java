package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;

public class ScoreboardCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        String name = data.getString("name");
        if (name == null) {
            if (entity instanceof PlayerEntity playerEntity) name = playerEntity.getName().getString();
            else name = entity.getUuidAsString();
        }

        Scoreboard scoreboard = entity.getWorld().getScoreboard();
        ScoreboardObjective scoreboardObjective = scoreboard.getObjective(data.getString("objective"));
        if (scoreboard.playerHasObjective(name, scoreboardObjective)) {
            int score = scoreboard.getPlayerScore(name, scoreboardObjective).getScore();
            return ((Comparison) data.get("comparison")).compare(score, data.getInt("compare_to"));
        }

        return false;
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
