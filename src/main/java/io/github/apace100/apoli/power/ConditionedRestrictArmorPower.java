package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.function.Predicate;

public class ConditionedRestrictArmorPower extends Power {

    private final HashMap<EquipmentSlot, Predicate<ItemStack>> armorConditions;
    private final int tickRate;

    public ConditionedRestrictArmorPower(PowerType<?> type, LivingEntity entity, HashMap<EquipmentSlot, Predicate<ItemStack>> armorConditions, int tickRate) {
        super(type, entity);
        this.armorConditions = armorConditions;
        this.setTicking(true);
        this.tickRate = tickRate;
    }

    public boolean canEquip(ItemStack itemStack, EquipmentSlot slot) {
        return !armorConditions.get(slot).test(itemStack);
    }

    @Override
    public void tick() {
        if(entity.age % tickRate == 0 && this.isActive()) {
            for(EquipmentSlot slot : armorConditions.keySet()) {
                ItemStack equippedItem = entity.getEquippedStack(slot);
                if(!equippedItem.isEmpty()) {
                    if(!canEquip(equippedItem, slot)) {
                        // TODO: Prefer putting armor into inv instead of dropping, if inv available
                        entity.dropStack(equippedItem, entity.getEyeHeight(entity.getPose()));
                        entity.equipStack(slot, ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("conditioned_restrict_armor"),
            new SerializableData()
                .add("head", ApoliDataTypes.ITEM_CONDITION, null)
                .add("chest", ApoliDataTypes.ITEM_CONDITION, null)
                .add("legs", ApoliDataTypes.ITEM_CONDITION, null)
                .add("feet", ApoliDataTypes.ITEM_CONDITION, null)
                .add("offhand", ApoliDataTypes.ITEM_CONDITION, null)
                .add("tick_rate", SerializableDataTypes.INT, 80),
            data ->
                (type, player) -> {
                    HashMap<EquipmentSlot, Predicate<ItemStack>> restrictions = new HashMap<>();
                    if(data.isPresent("head")) {
                        restrictions.put(EquipmentSlot.HEAD, (ConditionFactory<ItemStack>.Instance)data.get("head"));
                    }
                    if(data.isPresent("chest")) {
                        restrictions.put(EquipmentSlot.CHEST, (ConditionFactory<ItemStack>.Instance)data.get("chest"));
                    }
                    if(data.isPresent("legs")) {
                        restrictions.put(EquipmentSlot.LEGS, (ConditionFactory<ItemStack>.Instance)data.get("legs"));
                    }
                    if(data.isPresent("feet")) {
                        restrictions.put(EquipmentSlot.FEET, (ConditionFactory<ItemStack>.Instance)data.get("feet"));
                    }
                    if(data.isPresent("offhand")) {
                        restrictions.put(EquipmentSlot.OFFHAND, (ConditionFactory<ItemStack>.Instance)data.get("offhand"));
                    }
                    return new ConditionedRestrictArmorPower(type, player, restrictions, data.getInt("tick_rate"));
                })
            .allowCondition();
    }
}
