package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.loot.context.ApoliLootContextTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ModifyAction {

    public static void action(SerializableData.Instance data, Pair<World, ItemStack> worldAndStack) {

        MinecraftServer server = worldAndStack.getLeft().getServer();
        if (server == null) return;

        Identifier itemModifierId = data.get("modifier");
        LootFunction itemModifier = server.getLootManager().getElement(LootDataType.ITEM_MODIFIERS, itemModifierId);
        if (itemModifier == null) {
            Apoli.LOGGER.warn("Unknown item modifier (\"" + itemModifierId + "\") used in `modify` item action type");
            return;
        }

        RegistryKey<World> dimension = data.get("dimension");
        ServerWorld world = server.getWorld(dimension);
        if (world == null) {
            Apoli.LOGGER.warn("Unknown dimension (\"" + dimension.getValue() + "\") used in `modify` item action type!");
        }

        ItemStack stack = worldAndStack.getRight();
        Entity stackHolder = stack.getHolder();
        if (stackHolder != null) {
            Apoli.LOGGER.warn("Using the dimension of the stack holder instead.");
            world = (ServerWorld) stackHolder.getWorld();
        } else {
            Apoli.LOGGER.warn("Using the `minecraft:overworld` dimension instead.");
            world = server.getOverworld();
        }

        Vec3d pos = data.isPresent("position") ? data.get("position") : world.getSpawnPos().toCenterPos();
        BlockPos blockPos = BlockPos.ofFloored(pos);

        LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(world)
            .add(LootContextParameters.ORIGIN, pos)
            .add(LootContextParameters.TOOL, stack)
            .addOptional(LootContextParameters.THIS_ENTITY, stackHolder)
            .addOptional(LootContextParameters.BLOCK_STATE, world.getBlockState(blockPos))
            .addOptional(LootContextParameters.BLOCK_ENTITY, world.getBlockEntity(blockPos))
            .build(ApoliLootContextTypes.ANY);
        LootContext lootContext = new LootContext.Builder(lootContextParameterSet)
            .build(null);

        ItemStack newStack = itemModifier.apply(stack, lootContext);
        ((MutableItemStack) stack).setFrom(newStack);

    }

    public static ActionFactory<Pair<World, ItemStack>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("modify"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.IDENTIFIER)
                .add("position", SerializableDataTypes.VECTOR, null)
                .add("dimension", SerializableDataTypes.DIMENSION, World.OVERWORLD),
            ModifyAction::action
        );
    }

}
