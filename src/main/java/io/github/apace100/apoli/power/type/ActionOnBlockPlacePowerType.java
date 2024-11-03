package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

public class ActionOnBlockPlacePowerType extends ActiveInteractionPowerType {

    public static final TypedDataObjectFactory<ActionOnBlockPlacePowerType> DATA_FACTORY = ActiveInteractionPowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("place_to_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("place_on_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("place_to_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("place_on_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class)),
        (data, heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, condition) -> new ActionOnBlockPlacePowerType(
            data.get("entity_action"),
            data.get("place_to_action"),
            data.get("place_on_action"),
            data.get("place_to_condition"),
            data.get("place_on_condition"),
            data.get("directions"),
            heldItemAction,
            heldItemCondition,
            resultItemAction,
            resultStack,
            hands,
            priority,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("place_to_action", powerType.placeToAction)
            .set("place_on_action", powerType.placeOnAction)
            .set("place_to_condition", powerType.placeToCondition)
            .set("place_on_condition", powerType.placeOnCondition)
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<BlockAction> placeToAction;
    private final Optional<BlockAction> placeOnAction;

    private final Optional<BlockCondition> placeToCondition;
    private final Optional<BlockCondition> placeOnCondition;

    private final EnumSet<Direction> directions;

    public ActionOnBlockPlacePowerType(Optional<EntityAction> entityAction, Optional<BlockAction> placeToAction, Optional<BlockAction> placeOnAction, Optional<BlockCondition> placeToCondition, Optional<BlockCondition> placeOnCondition, EnumSet<Direction> directions, Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, int priority, Optional<EntityCondition> condition) {
        super(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, ActionResult.SUCCESS, priority, condition);
        this.entityAction = entityAction;
        this.placeToAction = placeToAction;
        this.placeOnAction = placeOnAction;
        this.placeToCondition = placeToCondition;
        this.placeOnCondition = placeOnCondition;
        this.directions = directions;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ACTION_ON_BLOCK_PLACE;
    }

    public boolean shouldExecute(ItemStack heldStack, Hand hand, BlockPos toPos, BlockPos onPos, Direction direction) {
        return super.shouldExecute(hand, heldStack)
            && directions.contains(direction)
            && placeOnCondition.map(condition -> condition.test(getHolder().getWorld(), onPos)).orElse(true)
            && placeToCondition.map(condition -> condition.test(getHolder().getWorld(), toPos)).orElse(true);
    }

    public void executeOtherActions(BlockPos toPos, BlockPos onPos, Direction direction) {

        placeOnAction.ifPresent(action -> action.execute(getHolder().getWorld(), onPos, Optional.of(direction)));
        placeToAction.ifPresent(action -> action.execute(getHolder().getWorld(), toPos, Optional.of(direction)));

        entityAction.ifPresent(action -> action.execute(getHolder()));

    }

    public void executeItemActions(Hand hand) {

        if (getHolder() instanceof PlayerEntity playerEntity) {
            this.performActorItemStuff(playerEntity, hand);
        }

    }

}
