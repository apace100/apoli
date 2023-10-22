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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventBlockPlacePower extends ActiveInteractionPower {

    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> placeToAction;
    private final Consumer<Triple<World, BlockPos, Direction>> placeOnAction;

    private final Predicate<CachedBlockPosition> placeToCondition;
    private final Predicate<CachedBlockPosition> placeOnCondition;

    private final EnumSet<Direction> directions;

    public PreventBlockPlacePower(PowerType<?> powerType, LivingEntity livingEntity, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> placeToAction, Consumer<Triple<World, BlockPos, Direction>> placeOnAction, Predicate<Pair<World, ItemStack>> itemCondition, Predicate<CachedBlockPosition> placeToCondition, Predicate<CachedBlockPosition> placeOnCondition, EnumSet<Direction> directions, EnumSet<Hand> hands, ItemStack resultStack, Consumer<Pair<World, ItemStack>> resultItemAction, Consumer<Pair<World, ItemStack>> heldItemAction, int priority) {
        super(powerType, livingEntity, hands, ActionResult.FAIL, itemCondition, heldItemAction, resultStack, resultItemAction, priority);
        this.entityAction = entityAction;
        this.placeToAction = placeToAction;
        this.placeOnAction = placeOnAction;
        this.placeToCondition = placeToCondition;
        this.placeOnCondition = placeOnCondition;
        this.directions = directions;
    }

    public boolean doesPrevent(ItemStack heldStack, Hand hand, BlockPos toPos, BlockPos onPos, Direction direction) {
        return (super.shouldExecute(hand, heldStack) && directions.contains(direction))
            && ((placeOnCondition == null || placeOnCondition.test(new CachedBlockPosition(entity.getWorld(), onPos, true)))
            && (placeToCondition == null || placeToCondition.test(new CachedBlockPosition(entity.getWorld(), toPos, true))));
    }

    public void executeActions(Hand hand, BlockPos toPos, BlockPos onPos, Direction direction) {

        if (placeOnAction != null) {
            placeOnAction.accept(Triple.of(entity.getWorld(), onPos, direction));
        }

        if (placeToAction != null) {
            placeToAction.accept(Triple.of(entity.getWorld(), toPos, direction));
        }

        if (entityAction != null) {
            entityAction.accept(entity);
        }

        if (entity instanceof PlayerEntity playerEntity) {
            performActorItemStuff(this, playerEntity, hand);
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("prevent_block_place"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("place_to_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("place_on_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("place_to_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("place_on_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class))
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(Hand.class))
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("held_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new PreventBlockPlacePower(
                powerType,
                livingEntity,
                data.get("entity_action"),
                data.get("place_to_action"),
                data.get("place_on_action"),
                data.get("item_condition"),
                data.get("place_to_condition"),
                data.get("place_on_condition"),
                data.get("directions"),
                data.get("hands"),
                data.get("result_stack"),
                data.get("result_item_action"),
                data.get("held_item_action"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
