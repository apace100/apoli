package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.Optional;

public class LightLevelBlockConditionType extends BlockConditionType {

    public static final TypedDataObjectFactory<LightLevelBlockConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("light_type", SerializableDataType.enumValue(LightType.class).optional(), Optional.empty())
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
        data -> new LightLevelBlockConditionType(
            data.get("light_type"),
            data.get("comparison"),
            data.get("compare_to")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("light_type", conditionType.lightType)
            .set("comparison", conditionType.comparison)
            .set("compare_to", conditionType.compareTo)
    );

    private final Optional<LightType> lightType;

    private final Comparison comparison;
    private final int compareTo;

    public LightLevelBlockConditionType(Optional<LightType> lightType, Comparison comparison, int compareTo) {
        this.lightType = lightType;
        this.comparison = comparison;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(World world, BlockPos pos) {

        int lightLevel = lightType
            .map(_lightType -> world.getLightLevel(_lightType, pos))
            .orElseGet(() -> world.getLightLevel(pos));

        return comparison.compare(lightLevel, compareTo);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.LIGHT_LEVEL;
    }

}
