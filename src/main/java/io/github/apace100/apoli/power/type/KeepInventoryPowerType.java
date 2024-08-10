package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class KeepInventoryPowerType extends PowerType {

    private final Predicate<Pair<World, ItemStack>> keepItemCondition;
    private final Set<Integer> slots;

    private ItemStack[] savedStacks;

    public KeepInventoryPowerType(Power power, LivingEntity entity, Predicate<Pair<World, ItemStack>> keepItemCondition, Collection<Integer> slots) {
        super(power, entity);
        this.keepItemCondition = keepItemCondition;
        if(slots == null) {
            this.slots = null;
        } else {
            this.slots = new HashSet<>(slots);
        }
    }

    public void preventItemsFromDropping(Inventory inventory) {
        savedStacks = new ItemStack[inventory.size()];
        for(int i = 0; i < inventory.size(); i++) {
            if(slots != null && !slots.contains(i)) {
                continue;
            }
            ItemStack stack = inventory.getStack(i);
            if(!stack.isEmpty()) {
                if(keepItemCondition == null || keepItemCondition.test(new Pair<>(entity.getWorld(), stack))) {
                    savedStacks[i] = stack;
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public void restoreSavedItems(Inventory inventory) {
        if(savedStacks == null) {
            Apoli.LOGGER.error(KeepInventoryPowerType.class.getSimpleName() +
                ": Tried to restore items without having saved any on entity \""
                + entity.getName().getString() + "\". Power may not have functioned correctly.");
            return;
        }
        if(inventory.size() != savedStacks.length) {
            Apoli.LOGGER.error(KeepInventoryPowerType.class.getSimpleName() +
                ": Tried to restore items with differently sized inventory on entity \""
                + entity.getName().getString() + "\". Items may have been lost.");
        }
        for(int i = 0; i < inventory.size() && i < savedStacks.length; i++) {
            if(savedStacks[i] != null && !savedStacks[i].isEmpty()) {
                inventory.setStack(i, savedStacks[i]);
            }
        }
        savedStacks = null;
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("keep_inventory"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("slots", SerializableDataTypes.INTS, null),
            data -> (power, entity) -> new KeepInventoryPowerType(power, entity,
                data.get("item_condition"),
                data.get("slots")
            )
        ).allowCondition();
    }
}
