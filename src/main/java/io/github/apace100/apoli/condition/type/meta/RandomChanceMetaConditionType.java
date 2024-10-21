package io.github.apace100.apoli.condition.type.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.apoli.util.context.TypeConditionContext;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.random.Random;

import java.util.function.Function;

public interface RandomChanceMetaConditionType {

    float chance();

    default boolean testCondition() {
        return Random.create().nextFloat() < chance();
    }

    static <T extends TypeConditionContext, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>, M extends AbstractConditionType<T, C> & RandomChanceMetaConditionType> ConditionConfiguration<M> createConfiguration(Function<Float, M> constructor) {
        return ConditionConfiguration.of(
            Apoli.identifier("random_chance"),
            new SerializableData()
                .add("chance", SerializableDataType.boundNumber(SerializableDataTypes.FLOAT, 0F, 1F)),
            data -> constructor.apply(
                data.get("chance")
            ),
            (m, serializableData) -> serializableData.instance()
                .set("chance", m.chance())
        );
    }

}
