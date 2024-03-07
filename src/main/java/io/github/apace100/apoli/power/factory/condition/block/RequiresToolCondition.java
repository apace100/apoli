package io.github.apace100.apoli.power.factory.condition.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;

public class RequiresToolCondition {
    public static boolean condition(SerializableData.Instance data, CachedBlockPosition block) {
        return block.getBlockState().isToolRequired();
    }

    public static ConditionFactory<CachedBlockPosition> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("requires_tool"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            RequiresToolCondition::condition
        );
    }
}
