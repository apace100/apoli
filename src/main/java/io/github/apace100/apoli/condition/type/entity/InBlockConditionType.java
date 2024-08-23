package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;

import java.util.function.Predicate;

public class InBlockConditionType {

    public static boolean condition(Entity entity, Predicate<CachedBlockPosition> blockCondition) {
        return blockCondition.test(new CachedBlockPosition(entity.getWorld(), entity.getBlockPos(), true));
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("in_block"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION),
            (data, entity) -> condition(entity,
                data.get("block_condition")
            )
        );
    }

}
