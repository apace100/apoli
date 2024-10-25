package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class ActiveInteractionPowerType extends InteractionPowerType implements Prioritized<ActiveInteractionPowerType> {

    private final int priority;

    public ActiveInteractionPowerType(Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, ActionResult actionResult, int priority, Optional<EntityCondition> condition) {
        super(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, condition);
        this.priority = priority;
    }

    public ActiveInteractionPowerType(Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, ActionResult actionResult, int priority) {
        this(heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, priority, Optional.empty());
    }

    public int getPriority() {
        return priority;
    }

    public static <T extends ActiveInteractionPowerType> TypedDataObjectFactory<T> createConditionedDataFactory(SerializableData serializableData, FromData<T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
        return InteractionPowerType.createConditionedDataFactory(
            serializableData
                .add("priority", SerializableDataTypes.INT, 0),
            (data, heldItemAction, heldItemCondition, resultItemAction, resultStack, hands, actionResult, condition) -> fromData.apply(
                data,
                heldItemAction,
                heldItemCondition,
                resultItemAction,
                resultStack,
                hands,
                actionResult,
                data.get("priority"),
                condition
            ),
            (t, _serializableData) -> toData.apply(t, _serializableData)
                .set("priority", t.getPriority())
        );
    }

    @FunctionalInterface
    public interface FromData<T extends ActiveInteractionPowerType> {
        T apply(SerializableData.Instance data, Optional<ItemAction> heldItemAction, Optional<ItemCondition> heldItemCondition, Optional<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<Hand> hands, ActionResult actionResult, int priority, Optional<EntityCondition> condition);
    }

}
