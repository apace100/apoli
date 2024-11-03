package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Optional;

public class PreventBeingUsedPowerType extends InteractionPowerType {

    public static final TypedDataObjectFactory<PreventBeingUsedPowerType> DATA_FACTORY = InteractionPowerType.createConditionedDataFactory(
        new SerializableData()
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, condition) -> new PreventBeingUsedPowerType(
            data.get("bientity_action"),
            data.get("bientity_condition"),
            heldItemAction,
            heldItemCondition,
            resultItemAction,
            resultStack,
            hands,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("bientity_action", powerType.biEntityAction)
            .set("bientity_condition", powerType.biEntityCondition)
    );

    private final Optional<BiEntityAction> biEntityAction;
    private final Optional<BiEntityCondition> biEntityCondition;

    public PreventBeingUsedPowerType(Optional<BiEntityAction> biEntityAction, Optional<BiEntityCondition> biEntityCondition, Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, Optional<EntityCondition> condition) {
        super(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, ActionResult.FAIL, condition);
        this.biEntityAction = biEntityAction;
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_BEING_USED;
    }

    public boolean doesApply(PlayerEntity other, Hand hand, ItemStack heldStack) {
        return shouldExecute(hand, heldStack)
            && biEntityCondition.map(condition -> condition.test(other, getHolder())).orElse(true);
    }

    public ActionResult executeAction(PlayerEntity other, Hand hand) {

        biEntityAction.ifPresent(action -> action.execute(other, getHolder()));
        this.performActorItemStuff(other, hand);

        return this.getActionResult();

    }

}
