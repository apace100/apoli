package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import org.jetbrains.annotations.Nullable;

public class BlockStateConditionType {

    public static boolean condition(BlockState blockState, String propertyName, @Nullable Comparison comparison, @Nullable Integer compareTo, @Nullable Boolean value, @Nullable String enumName) {

        var propertyValue = blockState.getProperties()
            .stream()
            .filter(prop -> prop.getName().equals(propertyName))
            .map(blockState::get)
            .findFirst()
            .orElse(null);

        return switch (propertyValue) {
            case Enum<?> enumValue when enumName != null ->
                enumValue.name().equalsIgnoreCase(enumName);
            case Boolean booleanValue when value != null ->
                booleanValue == value;
            case Integer intValue when comparison != null && compareTo != null ->
                comparison.compare(intValue, compareTo);
            case null, default ->
                propertyValue != null;
        };

    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("block_state"),
            new SerializableData()
                .add("property", SerializableDataTypes.STRING)
                .add("comparison", ApoliDataTypes.COMPARISON, null)
                .add("compare_to", SerializableDataTypes.INT, null)
                .add("value", SerializableDataTypes.BOOLEAN, null)
                .add("enum", SerializableDataTypes.STRING, null),
            (data, cachedBlock) -> condition(cachedBlock.getBlockState(),
                data.get("property"),
                data.get("comparison"),
                data.get("compare_to"),
                data.get("value"),
                data.get("enum")
            )
        );
    }

}
