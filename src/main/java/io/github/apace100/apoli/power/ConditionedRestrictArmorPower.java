package io.github.apace100.apoli.power;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
}
