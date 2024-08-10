package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.Vec3i;

import java.util.function.Predicate;

public class OffsetConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock, Predicate<CachedBlockPosition> blockCondition, Vec3i offset) {
        return blockCondition.test(new CachedBlockPosition(cachedBlock.getWorld(), cachedBlock.getBlockPos().add(offset), true));
    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("offset"),
            new SerializableData()
                .add("condition", ApoliDataTypes.BLOCK_CONDITION)
                .add("x", SerializableDataTypes.INT, 0)
                .add("y", SerializableDataTypes.INT, 0)
                .add("z", SerializableDataTypes.INT, 0),
            (data, cachedBlock) -> condition(cachedBlock,
                data.get("condition"),
                new Vec3i(data.get("x"), data.get("y"), data.get("z"))
            )
        );
    }

}
