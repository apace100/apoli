package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.IndexedStack;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class StartingEquipmentPowerType extends PowerType {

    public static final TypedDataObjectFactory<StartingEquipmentPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("stack", IndexedStack.DATA_TYPE, null)
            .addFunctionedDefault("stacks", IndexedStack.DATA_TYPE.list(1, Integer.MAX_VALUE), data -> MiscUtil.singletonListOrNull(data.get("stack")))
            .add("recurrent", SerializableDataTypes.BOOLEAN, false)
            .validate(MiscUtil.validateAnyFieldsPresent("stack", "stacks")),
        (data, condition) -> new StartingEquipmentPowerType(
            data.get("stacks"),
            data.get("recurrent"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("stacks", powerType.indexedStacks)
            .set("recurrent", powerType.recurrent)
    );

    private final List<IndexedStack> indexedStacks;
    private final boolean recurrent;

    public StartingEquipmentPowerType(List<IndexedStack> indexedStacks, boolean recurrent, Optional<EntityCondition> condition) {
        super(condition);
        this.indexedStacks = indexedStacks;
        this.recurrent = recurrent;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.STARTING_EQUIPMENT;
    }

    @Override
    public void onGained() {
        giveStacks();
    }

    @Override
    public void onRespawn() {
        if (recurrent) {
            giveStacks();
        }
    }

    private void giveStacks() {

        LivingEntity holder = getHolder();

        for (IndexedStack indexedStack : indexedStacks) {

            ItemStack stack = indexedStack.stack();
            int slotId = indexedStack.slotId();

            StackReference stackReference = holder.getStackReference(slotId);

            if (stackReference.get().isEmpty()) {
                stackReference.set(stack);
            }

            else if (holder instanceof PlayerEntity player) {
                player.getInventory().offerOrDrop(stack);
            }

            else {
                InventoryUtil.throwItem(holder, stack, true, true, 0);
            }

        }

    }

}
