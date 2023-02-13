package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.loot.context.ApoliLootContextTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootFunction;
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

        Identifier id = data.get("modifier");
        LootFunction lootFunction = server.getItemModifierManager().get(id);
        if (lootFunction == null) {
            Apoli.LOGGER.warn("Unknown \"" + id + "\" item modifier used in `modify` item action type!");
            return;
        }

        Vec3d pos = data.get("position");
        BlockPos blockPos = new BlockPos(pos);
        ItemStack stack = worldAndStack.getRight();
        Entity stackHolder = ((MutableItemStack) stack).getHolder();
        ServerWorld serverWorld = stackHolder != null ? (ServerWorld) stackHolder.getWorld() : server.getOverworld();
        LootContext lootContext = new LootContext.Builder(serverWorld)
            .parameter(LootContextParameters.ORIGIN, pos)
            .parameter(LootContextParameters.TOOL, stack)
            .parameter(LootContextParameters.BLOCK_STATE, serverWorld.getBlockState(blockPos))
            .parameter(LootContextParameters.BLOCK_ENTITY, serverWorld.getBlockEntity(blockPos))
            .optionalParameter(LootContextParameters.THIS_ENTITY, stackHolder)
            .build(ApoliLootContextTypes.ANY);

        ItemStack newStack = lootFunction.apply(stack, lootContext);
        ((MutableItemStack) stack).setFrom(newStack);

    }

    public static ActionFactory<Pair<World, ItemStack>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("modify"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.IDENTIFIER)
                .add("position", SerializableDataTypes.VECTOR, new Vec3d(0, 0, 0)),
            ModifyAction::action
        );
    }

}
