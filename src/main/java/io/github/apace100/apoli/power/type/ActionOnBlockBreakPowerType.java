package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnBlockBreakPowerType extends PowerType {

    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    private final Predicate<CachedBlockPosition> blockCondition;
    private final boolean onlyWhenHarvested;

    public ActionOnBlockBreakPowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction, Predicate<CachedBlockPosition> blockCondition, boolean onlyWhenHarvested) {
        super(power, entity);
        this.blockCondition = blockCondition;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
        this.onlyWhenHarvested = onlyWhenHarvested;
    }

    public boolean doesApply(CachedBlockPosition pos) {
        return blockCondition == null || blockCondition.test(pos);
    }

    public void executeActions(boolean successfulHarvest, BlockPos pos, Direction direction) {

        if (!successfulHarvest && onlyWhenHarvested) {
            return;
        }

        if (blockAction != null) {
            blockAction.accept(Triple.of(entity.getWorld(), pos, direction));
        }

        if (entityAction != null) {
            entityAction.accept(entity);
        }

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("action_on_block_break"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("only_when_harvested", SerializableDataTypes.BOOLEAN, false),
            data -> (power, entity) -> new ActionOnBlockBreakPowerType(power, entity,
                data.get("entity_action"),
                data.get("block_action"),
                data.get("block_condition"),
                data.get("only_when_harvested")
            )
        ).allowCondition();
    }

}
