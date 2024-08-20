package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ConditionedRestrictArmorPowerType extends RestrictArmorPowerType {

    private final int tickRate;

    private Integer startTicks;
    private Integer endTicks;

    private boolean wasActive;

    public ConditionedRestrictArmorPowerType(Power power, LivingEntity entity, Map<EquipmentSlot, Predicate<Pair<World, ItemStack>>> armorConditions, int tickRate) {
        super(power, entity, armorConditions);
        this.tickRate = tickRate;
        this.setTicking();
    }

    @Override
    public void tick() {

        if (this.isActive()) {

            if (startTicks == null) {

                this.startTicks = entity.age % tickRate;
                this.endTicks = null;

            }

            else if (entity.age % tickRate == startTicks) {
                dropEquippedStacks();
                wasActive = true;
            }

        }

        else if (wasActive) {

            if (endTicks == null) {

                this.endTicks = entity.age % tickRate;
                this.startTicks = null;

            }

            else if (entity.age % tickRate == endTicks) {
                wasActive = false;
            }

        }

    }

    @SuppressWarnings("RedundantMethodOverride")
    @Override
    public boolean shouldDrop(ItemStack stack, EquipmentSlot slot) {
        return super.doesRestrict(stack, slot);
    }

    @Override
    public boolean doesRestrict(ItemStack stack, EquipmentSlot slot) {
        return false;
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("conditioned_restrict_armor"),
            new SerializableData()
                .add("head", ApoliDataTypes.ITEM_CONDITION, null)
                .add("chest", ApoliDataTypes.ITEM_CONDITION, null)
                .add("legs", ApoliDataTypes.ITEM_CONDITION, null)
                .add("feet", ApoliDataTypes.ITEM_CONDITION, null)
                .add("tick_rate", SerializableDataTypes.POSITIVE_INT, 80),
            data -> (power, entity) -> {

                Map<EquipmentSlot, Predicate<Pair<World, ItemStack>>> restrictions = new HashMap<>();

                if (data.isPresent("head")) {
                    restrictions.put(EquipmentSlot.HEAD, data.get("head"));
                }

                if (data.isPresent("chest")) {
                    restrictions.put(EquipmentSlot.CHEST, data.get("chest"));
                }

                if (data.isPresent("legs")) {
                    restrictions.put(EquipmentSlot.LEGS, data.get("legs"));
                }

                if (data.isPresent("feet")) {
                    restrictions.put(EquipmentSlot.FEET, data.get("feet"));
                }

                return new ConditionedRestrictArmorPowerType(power, entity, restrictions, data.getInt("tick_rate"));

            }
        ).allowCondition();
    }

}
