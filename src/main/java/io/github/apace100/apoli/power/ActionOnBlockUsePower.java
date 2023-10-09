package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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

public class ActionOnBlockUsePower extends ActiveInteractionPower {

    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    private final Predicate<CachedBlockPosition> blockCondition;
    private final EnumSet<Direction> directions;

    public ActionOnBlockUsePower(PowerType<?> type, LivingEntity entity, EnumSet<Hand> hands, ActionResult actionResult, Predicate<Pair<World, ItemStack>> itemCondition, Consumer<Pair<World, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Pair<World, ItemStack>> resultItemAction, Consumer<Entity> entityAction, Predicate<CachedBlockPosition> blockCondition, EnumSet<Direction> directions, Consumer<Triple<World, BlockPos, Direction>> blockAction, int priority) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, resultItemAction, priority);
        this.entityAction = entityAction;
        this.blockCondition = blockCondition;
        this.directions = directions;
        this.blockAction = blockAction;
    }

    public boolean shouldExecute(BlockHitResult hitResult, Hand hand, ItemStack heldStack) {
        return super.shouldExecute(hand, heldStack)
            && directions.contains(hitResult.getSide())
            && (blockCondition == null || blockCondition.test(new CachedBlockPosition(entity.getWorld(), hitResult.getBlockPos(), true)));
    }

    public ActionResult executeAction(BlockHitResult hitResult, Hand hand) {

        if(blockAction != null) {
            blockAction.accept(Triple.of(entity.getWorld(), hitResult.getBlockPos(), hitResult.getSide()));
        }

        if(entityAction != null) {
            entityAction.accept(entity);
        }

        performActorItemStuff(this, (PlayerEntity) entity, hand);
        return getActionResult();

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("action_on_block_use"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class))
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(Hand.class))
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("held_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("action_result", SerializableDataTypes.ACTION_RESULT, ActionResult.SUCCESS)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new ActionOnBlockUsePower(
                powerType,
                livingEntity,
                data.get("hands"),
                data.get("action_result"),
                data.get("item_condition"),
                data.get("held_item_action"),
                data.get("result_stack"),
                data.get("result_item_action"),
                data.get("entity_action"),
                data.get("block_condition"),
                data.get("directions"),
                data.get("block_action"),
                data.get("priority"))
        ).allowCondition();
    }
}
