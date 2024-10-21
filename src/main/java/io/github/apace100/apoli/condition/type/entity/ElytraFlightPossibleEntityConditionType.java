package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
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

public class ElytraFlightPossibleEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<ElytraFlightPossibleEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("check_state", SerializableDataTypes.BOOLEAN, false)
            .add("check_ability", SerializableDataTypes.BOOLEAN, true),
        data -> new ElytraFlightPossibleEntityConditionType(
            data.get("check_state"),
            data.get("check_ability")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("check_state", conditionType.checkState)
            .set("check_ability", conditionType.checkAbility)
    );

    private final boolean checkState;
    private final boolean checkAbility;

    public ElytraFlightPossibleEntityConditionType(boolean checkState, boolean checkAbility) {
        this.checkState = checkState;
        this.checkAbility = checkAbility;
    }

    @Override
    public boolean test(Entity entity) {

        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        boolean state = true;
        boolean ability = true;
        boolean checked = false;

        if (checkState) {
            checked = true;
            state = !living.isOnGround()
                && !living.isFallFlying()
                && !living.isTouchingWater()
                && !living.hasStatusEffect(StatusEffects.LEVITATION);
        }

        if (checkAbility) {
            checked = true;
            ItemStack equippedChestStack = living.getEquippedStack(EquipmentSlot.CHEST);
            ability = (equippedChestStack.isOf(Items.ELYTRA) && ElytraItem.isUsable(equippedChestStack) || EntityElytraEvents.CUSTOM.invoker().useCustomElytra(living, false))
                && EntityElytraEvents.ALLOW.invoker().allowElytraFlight(living);
        }

        return checked
            && state
            && ability;

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.ELYTRA_FLIGHT_POSSIBLE;
    }

}
