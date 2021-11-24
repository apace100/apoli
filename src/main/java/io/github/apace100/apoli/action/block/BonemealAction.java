package io.github.apace100.apoli.action.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class BonemealAction {
    public static void action(SerializableData.Instance data, Triple<World, BlockPos, Direction> block) {
        World world = block.getLeft();
        BlockPos blockPos = block.getMiddle();

        BoneMealItem.useOnFertilizable(ItemStack.EMPTY, world, blockPos);
        if (world.isClient) {
            BoneMealItem.createParticles(world, blockPos, 0);
            return;
        };
    }

    public static ActionFactory<Triple<World, BlockPos, Direction>> createFactory() {
        return new ActionFactory<>(Apoli.identifier("bonemeal"),
                new SerializableData(),
                BonemealAction::action
        );
    }
}
