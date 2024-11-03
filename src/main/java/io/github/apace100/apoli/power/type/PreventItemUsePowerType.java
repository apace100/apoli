package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PreventItemUsePowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventItemUsePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new PreventItemUsePowerType(
            data.get("item_condition"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("item_condition", powerType.itemCondition)
    );

    private final Optional<ItemCondition> itemCondition;

    public PreventItemUsePowerType(Optional<ItemCondition> itemCondition, Optional<EntityCondition> condition) {
        super(condition);
        this.itemCondition = itemCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_ITEM_USE;
    }

    public boolean doesPrevent(ItemStack stack) {
        return itemCondition
            .map(condition -> condition.test(getHolder().getWorld(), stack))
            .orElse(true);
    }

}
