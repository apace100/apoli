package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class UsingItemCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.isUsingItem()) {
            return false;
        }

        Predicate<Pair<World, ItemStack>> itemCondition = data.get("item_condition");
        Hand activeHand = livingEntity.getActiveHand();

        ItemStack stackInHand = livingEntity.getStackInHand(activeHand);
        return itemCondition == null || itemCondition.test(new Pair<>(livingEntity.getWorld(), stackInHand));

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("using_item"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null),
            UsingItemCondition::condition
        );
    }

}
