package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ModifyHarvestPowerType extends PowerType implements Prioritized<ModifyHarvestPowerType>, Comparable<ModifyHarvestPowerType> {

    public static final TypedDataObjectFactory<ModifyHarvestPowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("allow", SerializableDataTypes.BOOLEAN, true)
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new ModifyHarvestPowerType(
            data.get("block_condition"),
            data.get("allow"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("block_condition", powerType.blockCondition)
            .set("allow", powerType.allow)
            .set("priority", powerType.getPriority())
    );

    private final Optional<BlockCondition> blockCondition;

    private final boolean allow;
    private final int priority;

    public ModifyHarvestPowerType(Optional<BlockCondition> blockCondition, boolean allow, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.blockCondition = blockCondition;
        this.allow = allow;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_HARVEST;
    }

    @Override
    public int compareTo(@NotNull ModifyHarvestPowerType other) {
        int priorityResult = Integer.compare(this.getPriority(), other.getPriority());
        return priorityResult != 0
            ? priorityResult
            : Boolean.compare(this.isHarvestAllowed(), other.isHarvestAllowed());
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(BlockView blockView, BlockPos pos) {
        return blockView instanceof World world && blockCondition
            .map(condition -> condition.test(world, pos))
            .orElse(true);
    }

    public boolean doesApply(CachedBlockPosition cachedBlock) {
        return doesApply(cachedBlock.getWorld(), cachedBlock.getBlockPos());
    }

    public boolean isHarvestAllowed() {
        return allow;
    }

}
