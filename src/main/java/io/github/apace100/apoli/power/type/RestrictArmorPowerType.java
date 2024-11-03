package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RestrictArmorPowerType extends PowerType {

    public static final TypedDataObjectFactory<RestrictArmorPowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        createSerializableData(),
        data -> {

            EnumMap<EquipmentSlot, Optional<ItemCondition>> conditions = Arrays.stream(EquipmentSlot.values())
                .filter(EquipmentSlot::isArmorSlot)
                .collect(Collectors.toMap(Function.identity(), slot -> data.get(slot.getName()), (o1, o2) -> o2, () -> new EnumMap<>(EquipmentSlot.class)));

            return new RestrictArmorPowerType(conditions);

        },
        (powerType, serializableData) -> {

            SerializableData.Instance data = serializableData.instance();
            powerType.armorConditions.forEach((equipmentSlot, itemCondition) -> data.set(equipmentSlot.getName(), itemCondition));

            return data;

        }
    );

    protected final EnumMap<EquipmentSlot, Optional<ItemCondition>> armorConditions;

    public RestrictArmorPowerType(EnumMap<EquipmentSlot, Optional<ItemCondition>> armorConditions, Optional<EntityCondition> condition) {
        super(condition);
        this.armorConditions = armorConditions;
    }

    public RestrictArmorPowerType(EnumMap<EquipmentSlot, Optional<ItemCondition>> armorConditions) {
        this(armorConditions, Optional.empty());
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.RESTRICT_ARMOR;
    }

    @Override
    public void onGained() {
        super.onGained();
        dropEquippedStacks();
    }

    public boolean doesRestrict(ItemStack stack, EquipmentSlot slot) {
        return armorConditions.getOrDefault(slot, Optional.empty())
            .map(condition -> condition.test(getHolder().getWorld(), stack))
            .orElse(false);
    }

    public void dropEquippedStacks() {

        LivingEntity holder = getHolder();
        if (holder.getWorld().isClient()) {
            return;
        }

        for (Map.Entry<EquipmentSlot, Optional<ItemCondition>> armorConditionEntry : armorConditions.entrySet()) {

            EquipmentSlot equipmentSlot = armorConditionEntry.getKey();
            Optional<ItemCondition> itemCondition = armorConditionEntry.getValue();

            ItemStack equippedStack = holder.getEquippedStack(equipmentSlot);

            //  TODO: Prefer inserting the armor items into the entity's inventory directly (if present)
            if (equippedStack.isEmpty() && itemCondition.map(condition -> condition.test(holder.getWorld(), equippedStack)).orElse(false)) {
                InventoryUtil.throwItem(holder, equippedStack, true, true, 0);
            }

        }

    }

    protected static SerializableData createSerializableData() {

        SerializableData serializableData = new SerializableData();
        Arrays.stream(EquipmentSlot.values())
            .filter(EquipmentSlot::isArmorSlot)
            .forEach(slot -> serializableData.add(slot.getName(), ItemCondition.DATA_TYPE.optional(), Optional.empty()));

        return serializableData;

    }

}
