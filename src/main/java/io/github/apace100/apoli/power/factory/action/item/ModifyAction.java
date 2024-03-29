package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.loot.context.ApoliLootContextTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class ModifyAction {

    public static void action(SerializableData.Instance data, Pair<World, StackReference> worldAndStack) {

        MinecraftServer server = worldAndStack.getLeft().getServer();
        if (server == null) {
            return;
        }

        Identifier itemModifierId = data.get("modifier");
        LootFunction itemModifier = server.getLootManager().getElement(LootDataType.ITEM_MODIFIERS, itemModifierId);
        if (itemModifier == null) {
            Apoli.LOGGER.warn("Unknown item modifier \"{}\" used in `modify` item action type!", itemModifierId);
            return;
        }

        ServerWorld overworld = server.getOverworld();

        StackReference stack = worldAndStack.getRight();
        BlockPos blockPos = overworld.getSpawnPos();
        Entity stackHolder = ((EntityLinkedItemStack) stack.get()).apoli$getEntity();

        LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(overworld)
            .add(LootContextParameters.ORIGIN, blockPos.toCenterPos())
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
                .add("modifier", SerializableDataTypes.IDENTIFIER),
            ModifyAction::action
        );
    }

}
