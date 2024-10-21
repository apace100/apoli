package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class RemoveEnchantmentItemActionType extends ItemActionType {

    public static final TypedDataObjectFactory<RemoveEnchantmentItemActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT.optional(), Optional.empty())
            .add("enchantments", SerializableDataTypes.ENCHANTMENT.list().optional(), Optional.empty())
            .add("levels", SerializableDataTypes.INT.optional(), Optional.empty())
            .add("reset_repair_cost", SerializableDataTypes.BOOLEAN, false),
        data -> new RemoveEnchantmentItemActionType(
			data.get("enchantment"),
            data.get("enchantments"),
			data.get("levels"),
			data.get("reset_repair_cost")
		),
        (actionType, serializableData) -> serializableData.instance()
            .set("enchantment", actionType.enchantmentKey)
            .set("enchantments", actionType.enchantmentKeys)
            .set("levels", actionType.levels)
            .set("reset_repair_cost", actionType.resetRepairCost)
    );

    private final Optional<RegistryKey<Enchantment>> enchantmentKey;
    private final Optional<List<RegistryKey<Enchantment>>> enchantmentKeys;

    private final List<RegistryKey<Enchantment>> allEnchantmentKeys;

    private final Optional<Integer> levels;
    private final boolean resetRepairCost;

    public RemoveEnchantmentItemActionType(Optional<RegistryKey<Enchantment>> enchantmentKey, Optional<List<RegistryKey<Enchantment>>> enchantmentKeys, Optional<Integer> levels, boolean resetRepairCost) {

        this.enchantmentKey = enchantmentKey;
        this.enchantmentKeys = enchantmentKeys;

        this.allEnchantmentKeys = new ObjectArrayList<>();

        this.enchantmentKey.ifPresent(this.allEnchantmentKeys::add);
        this.enchantmentKeys.ifPresent(this.allEnchantmentKeys::addAll);

        this.levels = levels;
        this.resetRepairCost = resetRepairCost;

    }

    @Override
	protected void execute(World world, StackReference stackReference) {

        ItemStack stack = stackReference.get();
        DynamicRegistryManager dynamicRegistries = world.getRegistryManager();

        if (!stack.hasEnchantments()) {
            return;
        }

        ItemEnchantmentsComponent oldEnchantments = stack.getEnchantments();
        ItemEnchantmentsComponent.Builder newEnchantments = new ItemEnchantmentsComponent.Builder(oldEnchantments);

        Registry<Enchantment> enchantmentRegistry = dynamicRegistries.get(RegistryKeys.ENCHANTMENT);
        for (RegistryKey<Enchantment> enchantmentKey : allEnchantmentKeys) {

            //  Since the registry keys are already validated, this should be fine.
            RegistryEntry<Enchantment> enchantment = enchantmentRegistry.entryOf(enchantmentKey);

            if (oldEnchantments.getEnchantments().contains(enchantment)) {
                newEnchantments.set(enchantment, levels.map(lvl -> oldEnchantments.getLevel(enchantment) - lvl).orElse(0));
            }

        }

        for (RegistryEntry<Enchantment> oldEnchantment : oldEnchantments.getEnchantments()) {

            if (!allEnchantmentKeys.isEmpty()) {
                break;
            }

            else {
                newEnchantments.set(oldEnchantment, levels.map(lvl -> oldEnchantments.getLevel(oldEnchantment) - lvl).orElse(0));
            }

        }

        stack.set(DataComponentTypes.ENCHANTMENTS, newEnchantments.build());
        if (resetRepairCost && !stack.hasEnchantments()) {
            stack.set(DataComponentTypes.REPAIR_COST, 0);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return ItemActionTypes.REMOVE_ENCHANTMENT;
    }

}
