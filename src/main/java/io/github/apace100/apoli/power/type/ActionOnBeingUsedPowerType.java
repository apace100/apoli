package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

public class ActionOnBeingUsedPowerType extends ActiveInteractionPowerType {

    public static final TypedDataObjectFactory<ActionOnBeingUsedPowerType> DATA_FACTORY = ActiveInteractionPowerType.createConditionedDataFactory(
        new SerializableData()
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, condition) -> new ActionOnBeingUsedPowerType(
            data.get("bientity_action"),
            data.get("bientity_condition"),
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
            .set("bientity_action", powerType.biEntityAction)
            .set("bientity_condition", powerType.biEntityCondition)
    );

    private final Optional<BiEntityAction> biEntityAction;
    private final Optional<BiEntityCondition> biEntityCondition;

    public ActionOnBeingUsedPowerType(Optional<BiEntityAction> biEntityAction, Optional<BiEntityCondition> biEntityCondition, Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, ActionResult actionResult, int priority, Optional<EntityCondition> condition) {
        super(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, condition);
        this.biEntityAction = biEntityAction;
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ACTION_ON_BEING_USED;
    }

    public boolean shouldExecute(PlayerEntity other, Hand hand, ItemStack heldStack, PriorityPhase priorityPhase) {
        return priorityPhase.test(this.getPriority())
            && super.shouldExecute(hand, heldStack)
            && biEntityCondition.map(condition -> condition.test(other, getHolder())).orElse(true);
    }

    public ActionResult executeAction(PlayerEntity other, Hand hand) {

        biEntityAction.ifPresent(action -> action.execute(other, getHolder()));
        performActorItemStuff(other, hand);

        return getActionResult();

    }

}
