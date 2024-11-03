package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.SavedBlockPosition;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ActionOnBlockBreakPowerType extends PowerType {

    public static final TypedDataObjectFactory<ActionOnBlockBreakPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("only_when_harvested", SerializableDataTypes.BOOLEAN, false),
        (data, condition) -> new ActionOnBlockBreakPowerType(
            data.get("entity_action"),
            data.get("block_action"),
            data.get("block_condition"),
            data.get("only_when_harvested"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("block_action", powerType.blockAction)
            .set("block_condition", powerType.blockCondition)
            .set("only_when_harvested", powerType.onlyWhenHarvested)
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<BlockAction> blockAction;

    private final Optional<BlockCondition> blockCondition;
    private final boolean onlyWhenHarvested;

    public ActionOnBlockBreakPowerType(Optional<EntityAction> entityAction, Optional<BlockAction> blockAction, Optional<BlockCondition> blockCondition, boolean onlyWhenHarvested, Optional<EntityCondition> condition) {
        super(condition);
        this.blockCondition = blockCondition;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
        this.onlyWhenHarvested = onlyWhenHarvested;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ACTION_ON_BLOCK_BREAK;
    }

    public boolean doesApply(SavedBlockPosition savedBlock) {
        return blockCondition
            .map(condition -> condition.test((World) savedBlock.getWorld(), savedBlock.getBlockPos()))
            .orElse(true);
    }

    public void executeActions(boolean successfulHarvest, BlockPos pos, Direction direction) {

        if (!successfulHarvest && onlyWhenHarvested) {
            return;
        }

        blockAction.ifPresent(action -> action.execute(getHolder().getWorld(), pos, Optional.of(direction)));
        entityAction.ifPresent(action -> action.execute(getHolder()));

    }

}
