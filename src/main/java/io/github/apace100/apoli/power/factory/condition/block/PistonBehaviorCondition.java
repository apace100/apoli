package io.github.apace100.apoli.power.factory.condition.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.pattern.CachedBlockPosition;

public class PistonBehaviorCondition {
    public static boolean condition(SerializableData.Instance data, CachedBlockPosition cachedBlockPosition) {
        return cachedBlockPosition.getBlockState().getPistonBehavior().equals(data.get("behavior"));
    }

    public static ConditionFactory<CachedBlockPosition> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("piston_behavior"),
            new SerializableData()
                .add("behavior", ApoliDataTypes.PISTON_BEHAVIOR),
            PistonBehaviorCondition::condition
        );
    }
}
