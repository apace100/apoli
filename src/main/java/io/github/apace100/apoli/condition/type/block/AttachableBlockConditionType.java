package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AttachableBlockConditionType extends BlockConditionType {

    @Override
    public boolean test(World world, BlockPos pos, BlockState blockState, Optional<BlockEntity> blockEntity) {

        for (Direction direction : Direction.values()) {

            BlockPos offsetPos = pos.offset(direction);

            if (world.isChunkLoaded(offsetPos) && world.getBlockState(offsetPos).isSideSolidFullSquare(world, pos, direction.getOpposite())) {
                return true;
            }

        }

        return false;

    }

    @Override
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BlockConditionTypes.ATTACHABLE;
    }

}
