package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.function.Predicate;

public class RestrictArmorPower extends Power {
    private final HashMap<EquipmentSlot, Predicate<Pair<World, ItemStack>>> armorConditions;

    public RestrictArmorPower(PowerType<?> type, LivingEntity entity, HashMap<EquipmentSlot, Predicate<Pair<World, ItemStack>>> armorConditions) {
        super(type, entity);
        this.armorConditions = armorConditions;
    }

    @Override
    public void onGained() {
        super.onGained();
        for(EquipmentSlot slot : armorConditions.keySet()) {
            ItemStack equippedItem = entity.getEquippedStack(slot);
            if(!equippedItem.isEmpty()) {
                if(!canEquip(equippedItem, slot)) {
                    // TODO: Prefer putting armor in inv instead of dropping when inv exists
                    entity.dropStack(equippedItem, entity.getEyeHeight(entity.getPose()));
                    entity.equipStack(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    public boolean canEquip(ItemStack itemStack, EquipmentSlot slot) {
        if (armorConditions.get(slot) == null) return true;
        return !armorConditions.get(slot).test(new Pair<>(entity.getWorld(), itemStack));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("restrict_armor"),
            new SerializableData()
                .add("head", ApoliDataTypes.ITEM_CONDITION, null)
                .add("chest", ApoliDataTypes.ITEM_CONDITION, null)
                .add("legs", ApoliDataTypes.ITEM_CONDITION, null)
                .add("feet", ApoliDataTypes.ITEM_CONDITION, null),
            data ->
                (type, player) -> {
                    HashMap<EquipmentSlot, Predicate<Pair<World, ItemStack>>> restrictions = new HashMap<>();
                    if(data.isPresent("head")) {
                        restrictions.put(EquipmentSlot.HEAD, data.get("head"));
                    }
                    if(data.isPresent("chest")) {
                        restrictions.put(EquipmentSlot.CHEST, data.get("chest"));
                    }
                    if(data.isPresent("legs")) {
                        restrictions.put(EquipmentSlot.LEGS, data.get("legs"));
                    }
                    if(data.isPresent("feet")) {
                        restrictions.put(EquipmentSlot.FEET, data.get("feet"));
                    }
                    return new RestrictArmorPower(type, player, restrictions);
                });
    }
}
