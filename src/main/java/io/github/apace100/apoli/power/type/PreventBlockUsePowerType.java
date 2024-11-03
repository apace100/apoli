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
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

public class PreventBlockUsePowerType extends ActiveInteractionPowerType {

    public static final TypedDataObjectFactory<PreventBlockUsePowerType> DATA_FACTORY = ActiveInteractionPowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("use_phases", ApoliDataTypes.BLOCK_USAGE_PHASE_SET, EnumSet.allOf(BlockUsagePhase.class))
            .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class)),
        (data, heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, condition) -> new PreventBlockUsePowerType(
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

    public PreventBlockUsePowerType(Optional<EntityAction> entityAction, Optional<BlockAction> blockAction, Optional<BlockCondition> blockCondition, EnumSet<BlockUsagePhase> usePhases, EnumSet<Direction> directions, Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, int priority, Optional<EntityCondition> condition) {
        super(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, ActionResult.FAIL, priority, condition);
        this.blockCondition = blockCondition;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
        this.directions = directions;
        this.usePhases = usePhases;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_BLOCK_USE;
    }

    public void executeActions(BlockHitResult hitResult, Hand hand) {

        LivingEntity holder = getHolder();

        blockAction.ifPresent(action -> action.execute(holder.getWorld(), hitResult.getBlockPos(), Optional.of(hitResult.getSide())));
        entityAction.ifPresent(action -> action.execute(holder));

        if (holder instanceof PlayerEntity player) {
            this.performActorItemStuff(player, hand);
        }

    }

    public boolean doesPrevent(BlockUsagePhase usePhase, BlockHitResult hitResult, ItemStack heldStack, Hand hand) {
        return usePhases.contains(usePhase)
            && directions.contains(hitResult.getSide())
            && super.shouldExecute(hand, heldStack)
            && blockCondition.map(condition -> condition.test(getHolder().getWorld(), hitResult.getBlockPos())).orElse(true);
    }

    public static boolean doesPrevent(Entity holder, BlockUsagePhase usePhase, BlockHitResult hitResult, ItemStack heldStack, Hand hand) {

        CallInstance<ActiveInteractionPowerType> aipci = new CallInstance<>();
        aipci.add(holder, PreventBlockUsePowerType.class, p -> p.doesPrevent(usePhase, hitResult, heldStack, hand));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {
            aipci.forEach(i, p -> ((PreventBlockUsePowerType) p).executeActions(hitResult, hand));
        }

        return !aipci.isEmpty();

    }

}
