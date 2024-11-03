package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.BlockUsagePhase;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

public class ActionOnBlockUsePowerType extends ActiveInteractionPowerType {

    public static final TypedDataObjectFactory<ActionOnBlockUsePowerType> DATA_FACTORY = ActiveInteractionPowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("use_phases", ApoliDataTypes.BLOCK_USAGE_PHASE_SET, EnumSet.allOf(BlockUsagePhase.class))
            .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class)),
        (data, heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, condition) -> new ActionOnBlockUsePowerType(
            data.get("entity_action"),
            data.get("block_action"),
            data.get("block_condition"),
            data.get("use_phases"),
            data.get("directions"),
            heldItemAction,
            heldItemCondition,
            resultItemAction,
            resultStack,
            hands,
            actionResult,
            priority,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("block_action", powerType.blockAction)
            .set("block_condition", powerType.blockCondition)
            .set("use_phases", powerType.usePhases)
            .set("directions", powerType.directions)
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<BlockAction> blockAction;

    private final Optional<BlockCondition> blockCondition;

    private final EnumSet<BlockUsagePhase> usePhases;
    private final EnumSet<Direction> directions;

    public ActionOnBlockUsePowerType(Optional<EntityAction> entityAction, Optional<BlockAction> blockAction, Optional<BlockCondition> blockCondition, EnumSet<BlockUsagePhase> usePhases, EnumSet<Direction> directions, Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, ActionResult actionResult, int priority, Optional<EntityCondition> condition) {
        super(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, condition);
        this.entityAction = entityAction;
        this.blockCondition = blockCondition;
        this.directions = directions;
        this.blockAction = blockAction;
        this.usePhases = usePhases;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ACTION_ON_BLOCK_USE;
    }

    public boolean shouldExecute(BlockUsagePhase usePhase, PriorityPhase priorityPhase, BlockHitResult hitResult, Hand hand, ItemStack heldStack) {
        return priorityPhase.test(this.getPriority())
            && usePhases.contains(usePhase)
            && directions.contains(hitResult.getSide())
            && super.shouldExecute(hand, heldStack)
            && blockCondition.map(condition -> condition.test(getHolder().getWorld(), hitResult.getBlockPos())).orElse(true);
    }

    public ActionResult executeAction(BlockHitResult hitResult, Hand hand) {

        blockAction.ifPresent(action -> action.execute(getHolder().getWorld(), hitResult.getBlockPos(), Optional.of(hitResult.getSide())));
        entityAction.ifPresent(action -> action.execute(getHolder()));

        if (getHolder() instanceof PlayerEntity player) {
            this.performActorItemStuff(player, hand);
        }

        return this.getActionResult();

    }

}
