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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

public class PreventBlockPlacePowerType extends ActiveInteractionPowerType {

    public static final TypedDataObjectFactory<PreventBlockPlacePowerType> DATA_FACTORY = ActiveInteractionPowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("place_to_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("place_on_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("place_to_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("place_on_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class))
            .add("priority", SerializableDataTypes.INT, 0),
        (data, heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, condition) -> new PreventBlockPlacePowerType(
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
            .set("directions", powerType.directions)
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<BlockAction> placeToAction;
    private final Optional<BlockAction> placeOnAction;

    private final Optional<BlockCondition> placeToCondition;
    private final Optional<BlockCondition> placeOnCondition;

    private final EnumSet<Direction> directions;

    public PreventBlockPlacePowerType(Optional<EntityAction> entityAction, Optional<BlockAction> placeToAction, Optional<BlockAction> placeOnAction, Optional<BlockCondition> placeToCondition, Optional<BlockCondition> placeOnCondition, EnumSet<Direction> directions, Optional<ItemAction> heldItemAction, Optional<ItemCondition> itemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, int priority, Optional<EntityCondition> condition) {
        super(heldItemAction, itemCondition, resultItemAction, resultStack, hands, ActionResult.FAIL, priority, condition);
        this.entityAction = entityAction;
        this.placeToAction = placeToAction;
        this.placeOnAction = placeOnAction;
        this.placeToCondition = placeToCondition;
        this.placeOnCondition = placeOnCondition;
        this.directions = directions;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_BLOCK_PLACE;
    }

    public boolean doesPrevent(ItemStack heldStack, Hand hand, BlockPos toPos, BlockPos onPos, Direction direction) {
        LivingEntity holder = getHolder();
        return super.shouldExecute(hand, heldStack)
            && directions.contains(direction)
            && placeOnCondition.map(condition -> condition.test(holder.getWorld(), onPos)).orElse(true)
            && placeToCondition.map(condition -> condition.test(holder.getWorld(), toPos)).orElse(true);
    }

    public void executeActions(Hand hand, BlockPos toPos, BlockPos onPos, Direction direction) {

        LivingEntity holder = getHolder();

        placeOnAction.ifPresent(action -> action.execute(holder.getWorld(), onPos, Optional.of(direction)));
        placeToAction.ifPresent(action -> action.execute(holder.getWorld(), toPos, Optional.of(direction)));

        entityAction.ifPresent(action -> action.execute(holder));

        if (holder instanceof PlayerEntity playerEntity) {
            performActorItemStuff(playerEntity, hand);
        }

    }

}
