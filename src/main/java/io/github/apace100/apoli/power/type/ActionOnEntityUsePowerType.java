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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

public class ActionOnEntityUsePowerType extends ActiveInteractionPowerType {

    public static final TypedDataObjectFactory<ActionOnEntityUsePowerType> DATA_FACTORY = ActiveInteractionPowerType.createConditionedDataFactory(
        new SerializableData()
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, condition) -> new ActionOnEntityUsePowerType(
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

    public ActionOnEntityUsePowerType(Optional<BiEntityAction> biEntityAction, Optional<BiEntityCondition> biEntityCondition, Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, ActionResult actionResult, int priority, Optional<EntityCondition> condition) {
        super(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, condition);
        this.biEntityAction = biEntityAction;
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ACTION_ON_ENTITY_USE;
    }

    public boolean shouldExecute(Entity other, Hand hand, ItemStack heldStack, PriorityPhase priorityPhase) {
        return priorityPhase.test(this.getPriority())
            && super.shouldExecute(hand, heldStack)
            && biEntityCondition.map(condition -> condition.test(getHolder(), other)).orElse(true);
    }

    public ActionResult executeAction(Entity other, Hand hand) {

        biEntityAction.ifPresent(action -> action.execute(getHolder(), other));
        this.performActorItemStuff((PlayerEntity) getHolder(), hand);

        return this.getActionResult();

    }

}
