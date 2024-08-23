package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ScoreboardConditionType {

    public static boolean condition(Entity entity, @Nullable String holderName, String objectiveName, Comparison comparison, int compareTo) {

        ScoreHolder scoreHolder = ScoreHolder.fromName(holderName != null ? holderName : entity.getNameForScoreboard());
        Scoreboard scoreboard = entity.getWorld().getScoreboard();

        return Optional.ofNullable(scoreboard.getNullableObjective(objectiveName))
            .map(objective -> scoreboard.getOrCreateScore(scoreHolder, objective))
            .map(scoreAccess -> comparison.compare(scoreAccess.getScore(), compareTo))
            .orElse(false);

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("scoreboard"),
            new SerializableData()
                .add("name", SerializableDataTypes.STRING, null)
                .add("objective", SerializableDataTypes.STRING)
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> condition(entity,
                data.get("name"),
                data.get("objective"),
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
