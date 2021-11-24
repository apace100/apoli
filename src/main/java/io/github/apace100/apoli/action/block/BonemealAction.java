package io.github.apace100.apoli.action.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.apache.commons.lang3.tuple.Triple;

public class BonemealAction {
    public static void action(SerializableData.Instance data, Triple<World, BlockPos, Direction> block) {
        World world = block.getLeft();
        BlockPos blockPos = block.getMiddle();
        Direction side = block.getRight();
        BlockPos blockPos2 = blockPos.offset(side);

        boolean spawnEffects = data.getBoolean("effects");

        if (BoneMealItem.useOnFertilizable(ItemStack.EMPTY, world, blockPos)) {
            if (spawnEffects && !world.isClient) {
                world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 0);
            }
        } else {
            BlockState blockState = world.getBlockState(blockPos);
            boolean bl = blockState.isSideSolidFullSquare(world, blockPos, side);
            if (bl && BoneMealItem.useOnGround(ItemStack.EMPTY, world, blockPos2, side)) {
                if (spawnEffects && !world.isClient) {
                    world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos2, 0);
                }
            }
        }
    }

    public static ActionFactory<Triple<World, BlockPos, Direction>> createFactory() {
        return new ActionFactory<>(Apoli.identifier("bonemeal"),
                new SerializableData()
                    .add("effects", SerializableDataTypes.BOOLEAN, true),
                BonemealAction::action
        );
    }
}
