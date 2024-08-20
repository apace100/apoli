package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
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

public class BoneMealActionType {

    public static void action(World world, BlockPos pos, Direction direction, boolean showEffects) {

        if (BoneMealItem.useOnFertilizable(ItemStack.EMPTY, world, pos)) {

            if (showEffects && !world.isClient) {
                world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 0);
            }

        }

        else {

            BlockState blockState = world.getBlockState(pos);
            BlockPos offsetPos = pos.offset(direction);

            boolean solidSide = blockState.isSideSolidFullSquare(world, pos, direction);

            if (solidSide && BoneMealItem.useOnGround(ItemStack.EMPTY, world, offsetPos, direction)) {

                if (showEffects && !world.isClient) {
                    world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, offsetPos, 0);
                }

            }

        }

    }

    public static ActionTypeFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("bonemeal"),
            new SerializableData()
                .add("effects", SerializableDataTypes.BOOLEAN, true),
            (data, block) -> action(block.getLeft(), block.getMiddle(), block.getRight(),
                data.get("effects")
            )
        );
    }

}
