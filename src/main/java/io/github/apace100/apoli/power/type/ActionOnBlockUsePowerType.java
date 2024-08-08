package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.BlockUsagePhase;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnBlockUsePowerType extends ActiveInteractionPowerType {

    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    private final Predicate<CachedBlockPosition> blockCondition;

    private final EnumSet<Direction> directions;
    private final EnumSet<BlockUsagePhase> usePhases;

    public ActionOnBlockUsePowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction, Consumer<Pair<World, StackReference>> heldItemAction, Consumer<Pair<World, StackReference>> resultItemAction, Predicate<CachedBlockPosition> blockCondition, Predicate<Pair<World, ItemStack>> itemCondition, ItemStack itemResult, EnumSet<Direction> directions, EnumSet<Hand> hands, EnumSet<BlockUsagePhase> usePhases, ActionResult actionResult, int priority) {
        super(power, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, resultItemAction, priority);
        this.entityAction = entityAction;
        this.blockCondition = blockCondition;
        this.directions = directions;
        this.blockAction = blockAction;
        this.usePhases = usePhases;
    }

    public boolean shouldExecute(BlockUsagePhase usePhase, PriorityPhase priorityPhase, BlockHitResult hitResult, Hand hand, ItemStack heldStack) {
        return priorityPhase.test(this.getPriority())
            && usePhases.contains(usePhase)
            && directions.contains(hitResult.getSide())
            && super.shouldExecute(hand, heldStack)
            && (blockCondition == null || blockCondition.test(new CachedBlockPosition(entity.getWorld(), hitResult.getBlockPos(), true)));
    }

    public ActionResult executeAction(BlockHitResult hitResult, Hand hand) {

        if (blockAction != null) {
            this.blockAction.accept(Triple.of(entity.getWorld(), hitResult.getBlockPos(), hitResult.getSide()));
        }

        if (entityAction != null) {
            this.entityAction.accept(entity);
        }

        if (entity instanceof PlayerEntity player) {
            this.performActorItemStuff(this, player, hand);
        }

        return this.getActionResult();

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("action_on_block_use"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("held_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class))
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(Hand.class))
                .add("usage_phases", ApoliDataTypes.BLOCK_USAGE_PHASE_SET, EnumSet.allOf(BlockUsagePhase.class))
                .add("action_result", SerializableDataTypes.ACTION_RESULT, ActionResult.SUCCESS)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new ActionOnBlockUsePowerType(power, entity,
                data.get("entity_action"),
                data.get("block_action"),
                data.get("held_item_action"),
                data.get("result_item_action"),
                data.get("block_condition"),
                data.get("item_condition"),
                data.get("result_stack"),
                data.get("directions"),
                data.get("hands"),
                data.get("usage_phases"),
                data.get("action_result"),
                data.get("priority")
            )
        ).allowCondition();
    }
}
