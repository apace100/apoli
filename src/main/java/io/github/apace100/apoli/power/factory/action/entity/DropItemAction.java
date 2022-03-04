package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.InventoryPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.InventoryType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class DropItemAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof PlayerEntity playerEntity)) return;

        InventoryType inventoryType = data.get("inventory_type");

        switch (inventoryType) {
            case ENDER_CHEST:
                EnderChestInventory enderChestInventory = playerEntity.getEnderChestInventory();
                drop(data, playerEntity, enderChestInventory);
                break;
            case PLAYER:
                PlayerInventory playerInventory = playerEntity.getInventory();
                drop(data, playerEntity, playerInventory);
                break;
            case POWER:
                if (!data.isPresent("power")) return;

                PowerType<?> targetPowerType = data.get("power");
                Power targetPower = PowerHolderComponent.KEY.get(playerEntity).getPower(targetPowerType);

                if (!(targetPower instanceof InventoryPower inventoryPower)) return;
                drop(data, playerEntity, inventoryPower);
        }
    }

    private static void drop(SerializableData.Instance data, PlayerEntity playerEntity, Inventory inventory) {

        Set<Integer> slots = null;
        if (data.isPresent("slots")) slots = new HashSet<>(data.get("slots"));

        boolean throwRandomly = data.getBoolean("throw_randomly");
        boolean retainOwnership = data.getBoolean("retain_ownership");
        Predicate<ItemStack> itemCondition = data.get("item_condition");

        for (int i = 0; i < inventory.size(); ++i) {
            if (slots != null && !slots.contains(i)) continue;
            ItemStack currentItemStack = inventory.getStack(i);
            if (!currentItemStack.isEmpty()) {
                if (itemCondition == null || itemCondition.test(currentItemStack)) {
                    playerEntity.dropItem(currentItemStack, throwRandomly, retainOwnership);
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("drop_item"),
            new SerializableData()
                .add("inventory_type", SerializableDataType.enumValue(InventoryType.class), InventoryType.PLAYER)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("slots", SerializableDataTypes.INTS, null)
                .add("power", ApoliDataTypes.POWER_TYPE, null)
                .add("throw_randomly", SerializableDataTypes.BOOLEAN, false)
                .add("retain_ownership", SerializableDataTypes.BOOLEAN, true),
            DropItemAction::action
        );
    }
}
