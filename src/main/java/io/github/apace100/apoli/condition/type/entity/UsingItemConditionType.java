package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class UsingItemConditionType {

    public static boolean condition(Entity entity, Predicate<Pair<World, ItemStack>> itemCondition) {

        if (!(entity instanceof LivingEntity living) || !living.isUsingItem()) {
            return false;
        }

        Hand activeHand = living.getActiveHand();
        ItemStack stackInHand = living.getStackInHand(activeHand);

        return itemCondition.test(new Pair<>(living.getWorld(), stackInHand));

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("using_item"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null),
            (data, entity) -> condition(entity,
                data.getOrElse("item_condition", worldAndStack -> true))
        );
    }

}
