package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class PreventItemUsePower extends Power {

    private final Predicate<Pair<World, ItemStack>> itemCondition;

    public PreventItemUsePower(PowerType<?> type, LivingEntity entity, Predicate<Pair<World, ItemStack>> itemCondition) {
        super(type, entity);
        this.itemCondition = itemCondition;
    }

    public boolean doesPrevent(ItemStack stack) {
        return itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("prevent_item_use"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null),
            data -> (powerType, LivingEntity) -> new PreventItemUsePower(
                powerType,
                LivingEntity,
                data.get("item_condition")
            )
        ).allowCondition();
    }

}
