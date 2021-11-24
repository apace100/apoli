package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnBlockUsePower extends InteractionPower {

    private final Predicate<CachedBlockPosition> blockCondition;
    private final EnumSet<Direction> directions;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    public ActionOnBlockUsePower(PowerType<?> type, LivingEntity entity, EnumSet<Hand> hands, ActionResult actionResult, Predicate<ItemStack> itemCondition, Consumer<Pair<World, ItemStack>> heldItemAction, ItemStack itemResult, Consumer<Pair<World, ItemStack>> resultItemAction, Predicate<CachedBlockPosition> blockCondition, EnumSet<Direction> directions, Consumer<Triple<World, BlockPos, Direction>> blockAction) {
        super(type, entity, hands, actionResult, itemCondition, heldItemAction, itemResult, resultItemAction);
        this.blockCondition = blockCondition;
        this.directions = directions;
        this.blockAction = blockAction;
    }


    public boolean shouldExecute(BlockPos blockPos, Direction direction, Hand hand, ItemStack heldStack) {
        if(!super.shouldExecute(hand, heldStack)) {
            return false;
        }
        if(!directions.contains(direction)) {
            return false;
        }
        return blockCondition == null || blockCondition.test(new CachedBlockPosition(entity.world, blockPos, true));
    }

    public ActionResult executeAction(BlockPos blockPos, Direction direction, Hand hand) {
        if(blockAction != null) {
            blockAction.accept(Triple.of(entity.world, blockPos, direction));
        }
        performActorItemStuff(this, (PlayerEntity) entity, hand);
        return getActionResult();
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("action_on_block_use"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class))
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(Hand.class))
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("held_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("action_result", SerializableDataTypes.ACTION_RESULT, ActionResult.SUCCESS),
            data ->
                (type, player) -> {
                    return new ActionOnBlockUsePower(type, player,
                        (EnumSet<Hand>)data.get("hands"),
                        (ActionResult)data.get("action_result"),
                        (Predicate<ItemStack>)data.get("item_condition"),
                        (Consumer<Pair<World, ItemStack>>)data.get("held_item_action"),
                        (ItemStack)data.get("result_stack"),
                        (Consumer<Pair<World, ItemStack>>)data.get("result_item_action"),
                        (Predicate<CachedBlockPosition>) data.get("block_condition"),
                        (EnumSet<Direction>) data.get("directions"),
                        (Consumer<Triple<World, BlockPos, Direction>>) data.get("block_action"));
                })
            .allowCondition();
    }
}
