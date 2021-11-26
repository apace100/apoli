package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ElytraFlightPossibleCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        if(!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }
        boolean ability = true;
        if(data.getBoolean("check_ability")) {
            ItemStack equippedChestItem = livingEntity.getEquippedStack(EquipmentSlot.CHEST);
            ability = equippedChestItem.isOf(Items.ELYTRA) && ElytraItem.isUsable(equippedChestItem);
            if (!ability && EntityElytraEvents.CUSTOM.invoker().useCustomElytra(livingEntity, false)) {
                ability = true;
            }
            if (!EntityElytraEvents.ALLOW.invoker().allowElytraFlight(livingEntity)) {
                ability = false;
            }
        }
        boolean state = true;
        if(data.getBoolean("check_state")) {
            state = !livingEntity.isOnGround() && !livingEntity.isFallFlying() && !livingEntity.isTouchingWater() && !livingEntity.hasStatusEffect(StatusEffects.LEVITATION);
        }
        return ability && state;
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("elytra_flight_possible"),
            new SerializableData()
                .add("check_state", SerializableDataTypes.BOOLEAN, false)
                .add("check_ability", SerializableDataTypes.BOOLEAN, true),
            ElytraFlightPossibleCondition::condition
        );
    }
}