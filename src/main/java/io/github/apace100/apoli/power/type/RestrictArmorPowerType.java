package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class RestrictArmorPowerType extends PowerType {

    protected final Map<EquipmentSlot, Predicate<Pair<World, ItemStack>>> armorConditions;

    public RestrictArmorPowerType(Power power, LivingEntity entity, Map<EquipmentSlot, Predicate<Pair<World, ItemStack>>> armorConditions) {
        super(power, entity);
        this.armorConditions = armorConditions;
    }

    @Override
    public void onGained() {
        super.onGained();
        dropEquippedStacks();
    }

    public void dropEquippedStacks() {

        for (EquipmentSlot slot : armorConditions.keySet()) {

            ItemStack equippedStack = entity.getEquippedStack(slot);

            if(!equippedStack.isEmpty() && this.shouldDrop(equippedStack, slot)) {
                // TODO: Prefer putting armor in inv instead of dropping when inv exists
                entity.dropStack(equippedStack, entity.getEyeHeight(entity.getPose()));
                entity.equipStack(slot, ItemStack.EMPTY);
            }

        }

    }

    public boolean shouldDrop(ItemStack stack, EquipmentSlot slot) {
        return this.doesRestrict(stack, slot);
    }

    public boolean doesRestrict(ItemStack stack, EquipmentSlot slot) {
        Predicate<Pair<World, ItemStack>> armorCondition = armorConditions.get(slot);
        return armorCondition != null && armorCondition.test(new Pair<>(entity.getWorld(), stack));
    }

    public static PowerTypeFactory<RestrictArmorPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("restrict_armor"),
            new SerializableData()
                .add("head", ApoliDataTypes.ITEM_CONDITION, null)
                .add("chest", ApoliDataTypes.ITEM_CONDITION, null)
                .add("legs", ApoliDataTypes.ITEM_CONDITION, null)
                .add("feet", ApoliDataTypes.ITEM_CONDITION, null),
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

                return new RestrictArmorPowerType(power, entity, restrictions);

            }
        );
    }

}
