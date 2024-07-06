package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.loot.context.ApoliLootContextTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.Optional;

public class ModifyAction {

    public static void action(SerializableData.Instance data, Pair<World, StackReference> worldAndStack) {

        if (!(worldAndStack.getLeft() instanceof ServerWorld serverWorld)) {
            return;
        }

        RegistryKey<LootFunction> itemModifierKey = data.get("modifier");
        LootFunction itemModifier = serverWorld.getRegistryManager().get(RegistryKeys.ITEM_MODIFIER).getOrThrow(itemModifierKey);

        StackReference stack = worldAndStack.getRight();
        Entity stackHolder = ((EntityLinkedItemStack) stack.get()).apoli$getEntity();

        LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(serverWorld)
            .add(LootContextParameters.ORIGIN, serverWorld.getSpawnPos().toCenterPos())
            .add(LootContextParameters.TOOL, stack.get())
            .addOptional(LootContextParameters.THIS_ENTITY, stackHolder)
            .build(ApoliLootContextTypes.ANY);
        LootContext lootContext = new LootContext.Builder(lootContextParameterSet).build(Optional.empty());

        ItemStack newStack = itemModifier.apply(stack.get(), lootContext);
        stack.set(newStack);

    }

    public static ItemActionFactory getFactory() {
        return new ItemActionFactory(
            Apoli.identifier("modify"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.ITEM_MODIFIER),
            ModifyAction::action
        );
    }

}
