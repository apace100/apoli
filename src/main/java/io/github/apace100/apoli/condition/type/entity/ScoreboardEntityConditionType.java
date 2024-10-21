package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;

import java.util.Optional;

public class ScoreboardEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<ScoreboardEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("name", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("objective", SerializableDataTypes.STRING)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new ScoreboardEntityConditionType(
            data.get("name"),
            data.get("objective"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("name", conditionType.name)
            .set("objective", conditionType.objective)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Optional<String> name;
    private final String objective;

    private final Comparison comparison;
    private final int compareTo;

    public ScoreboardEntityConditionType(Optional<String> name, String objective, Comparison comparison, int compareTo) {
        this.name = name;
        this.objective = objective;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(Entity entity) {

        ScoreHolder scoreHolder = ScoreHolder.fromName(name.orElse(entity.getNameForScoreboard()));
        Scoreboard scoreboard = entity.getWorld().getScoreboard();

        return Optional.ofNullable(scoreboard.getNullableObjective(objective))
            .flatMap(objective -> Optional.ofNullable(scoreboard.getScore(scoreHolder, objective)))
            .map(ReadableScoreboardScore::getScore)
            .map(score -> comparison.compare(score, compareTo))
            .orElse(false);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.SCOREBOARD;
    }

}
