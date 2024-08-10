package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
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

public class ElytraFlightPossibleConditionType {

    public static boolean condition(Entity entity, boolean checkState, boolean checkAbility) {

        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        boolean state = true;
        boolean ability = true;

        if (checkAbility) {
            ItemStack equippedChestStack = living.getEquippedStack(EquipmentSlot.CHEST);
            ability = (equippedChestStack.isOf(Items.ELYTRA) && ElytraItem.isUsable(equippedChestStack) || EntityElytraEvents.CUSTOM.invoker().useCustomElytra(living, false))
                && EntityElytraEvents.ALLOW.invoker().allowElytraFlight(living);
        }

        if (checkState) {
            state = !living.isOnGround()
                && !living.isFallFlying()
                && !living.isTouchingWater()
                && !living.hasStatusEffect(StatusEffects.LEVITATION);
        }

        return ability && state;

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(Apoli.identifier("elytra_flight_possible"),
            new SerializableData()
                .add("check_state", SerializableDataTypes.BOOLEAN, false)
                .add("check_ability", SerializableDataTypes.BOOLEAN, true),
            (data, entity) -> condition(entity,
                data.get("check_state"),
                data.get("check_ability")
            )
        );
    }
}
