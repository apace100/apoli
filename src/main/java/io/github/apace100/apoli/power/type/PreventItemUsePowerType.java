package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class PreventItemUsePowerType extends PowerType {

    private final Predicate<Pair<World, ItemStack>> itemCondition;

    public PreventItemUsePowerType(Power power, LivingEntity entity, Predicate<Pair<World, ItemStack>> itemCondition) {
        super(power, entity);
        this.itemCondition = itemCondition;
    }

    public boolean doesPrevent(ItemStack stack) {
        return itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack));
    }

    public static PowerTypeFactory<PreventItemUsePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_item_use"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null),
            data -> (power, entity) -> new PreventItemUsePowerType(power, entity,
                data.get("item_condition")
            )
        ).allowCondition();
    }

}
