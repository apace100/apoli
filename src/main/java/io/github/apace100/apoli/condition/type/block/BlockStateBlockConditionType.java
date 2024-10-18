package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStateBlockConditionType extends BlockConditionType {

    public static final DataObjectFactory<BlockStateBlockConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("property", SerializableDataTypes.STRING)
            .add("comparison", ApoliDataTypes.COMPARISON, null)
            .add("compare_to", SerializableDataTypes.INT, null)
            .add("value", SerializableDataTypes.BOOLEAN, null)
            .add("enum", SerializableDataTypes.STRING, null),
        data -> new BlockStateBlockConditionType(
            data.get("property"),
            data.get("comparison"),
            data.get("compare_to"),
            data.get("value"),
            data.get("enum")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("property", conditionType.property)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
            .set("value", conditionType.boolValue)
            .set("enum", conditionType.enumValue)
    );

    private final String property;

    private final Comparison comparison;
    private final Integer compareTo;

    private final Boolean boolValue;
    private final String enumValue;

    public BlockStateBlockConditionType(String property, Comparison comparison, Integer compareTo, Boolean boolValue, String enumValue) {
        this.property = property;
        this.comparison = comparison;
        this.compareTo = compareTo;
        this.boolValue = boolValue;
        this.enumValue = enumValue;
    }

    @Override
    public boolean test(World world, BlockPos pos) {

        BlockState blockState = world.getBlockState(pos);
        var propertyValue = blockState.getProperties()
            .stream()
            .filter(prop -> prop.getName().equals(property))
            .map(blockState::get)
            .findFirst()
            .orElse(null);

        return switch (propertyValue) {
            case Enum<?> enumProp when enumValue != null ->
                enumProp.name().equalsIgnoreCase(enumValue);
            case Boolean boolProp when boolValue != null ->
                boolProp == boolValue;
            case Integer intProp when comparison != null && compareTo != null ->
                comparison.compare(intProp, compareTo);
            case null, default ->
                propertyValue != null;
        };

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.BLOCK_STATE;
    }

}
