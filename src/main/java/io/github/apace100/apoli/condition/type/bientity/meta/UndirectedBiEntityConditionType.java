package io.github.apace100.apoli.condition.type.bientity.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class UndirectedBiEntityConditionType extends BiEntityConditionType {

    public static final DataObjectFactory<UndirectedBiEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("condition", BiEntityCondition.DATA_TYPE),
        data -> new UndirectedBiEntityConditionType(
            data.get("condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("condition", conditionType.biEntityCondition)
    );

    private final BiEntityCondition biEntityCondition;

    public UndirectedBiEntityConditionType(BiEntityCondition biEntityCondition) {
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.UNDIRECTED;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return biEntityCondition.test(actor, target)
            || biEntityCondition.test(target, actor);
    }

    public static boolean condition(Entity actor, Entity target, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        return biEntityCondition.test(new Pair<>(actor, target))
            || biEntityCondition.test(new Pair<>(target, actor));
    }

    public static ConditionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("undirected"),
            new SerializableData()
                .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            (data, actorAndTarget) -> condition(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                data.get("condition")
            )
        );
    }

}
