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
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyInventoryAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof PlayerEntity playerEntity)) return;

        InventoryType inventoryType = data.get("inventory_type");

        switch (inventoryType) {
            case ENDER_CHEST:
                EnderChestInventory enderChestInventory = playerEntity.getEnderChestInventory();
                modify(data, playerEntity, enderChestInventory);
                break;
            case PLAYER:
                PlayerInventory playerInventory = playerEntity.getInventory();
                modify(data, playerEntity, playerInventory);
                break;
            case POWER:
                if (!data.isPresent("power")) return;

                PowerType<?> targetPowerType = data.get("power");
                Power targetPower = PowerHolderComponent.KEY.get(playerEntity).getPower(targetPowerType);

                if (!(targetPower instanceof InventoryPower inventoryPower)) return;
                modify(data, playerEntity, inventoryPower);
        }
    }

    private static void modify(SerializableData.Instance data, PlayerEntity playerEntity, Inventory inventory) {

        Set<Integer> slots = null;
        if (data.isPresent("slots")) slots = new HashSet<>(data.get("slots"));

        Consumer<Entity> entityAction = data.get("entity_action");
        Predicate<ItemStack> itemCondition = data.get("item_condition");
        ActionFactory<Pair<World, ItemStack>>.Instance itemAction = data.get("item_action");

        for (int i = 0; i < inventory.size(); ++i) {
            if (slots != null && !slots.contains(i)) continue;
            ItemStack currentItemStack = inventory.getStack(i);
            if (!currentItemStack.isEmpty()) {
                if (itemCondition == null || itemCondition.test(currentItemStack)) {
                    if (entityAction != null) entityAction.accept(playerEntity);
                    itemAction.accept(new Pair<>(playerEntity.world, currentItemStack));
                }
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("modify_inventory"),
            new SerializableData()
                .add("inventory_type", SerializableDataType.enumValue(InventoryType.class), InventoryType.PLAYER)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("slots", SerializableDataTypes.INTS, null)
                .add("power", ApoliDataTypes.POWER_TYPE, null),
            ModifyInventoryAction::action
        );
    }
}
