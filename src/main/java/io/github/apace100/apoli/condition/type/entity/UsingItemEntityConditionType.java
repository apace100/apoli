package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.Optional;

public class UsingItemEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<UsingItemEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty()),
        data -> new UsingItemEntityConditionType(
            data.get("item_condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("item_condition", conditionType.itemCondition)
    );

    private final Optional<ItemCondition> itemCondition;

    public UsingItemEntityConditionType(Optional<ItemCondition> itemCondition) {
        this.itemCondition = AbstractCondition.setPowerType(itemCondition, getPowerType());
    }

    @Override
    public boolean test(Entity entity) {

        if (entity instanceof LivingEntity livingEntity && livingEntity.isUsingItem()) {

            Hand activeHand = livingEntity.getActiveHand();
            ItemStack stackInHand = livingEntity.getStackInHand(activeHand);

            return itemCondition
                .map(condition -> condition.test(entity.getWorld(), stackInHand))
                .orElse(true);

        }

        else {
            return false;
        }

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.USING_ITEM;
    }

}
