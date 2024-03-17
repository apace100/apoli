package io.github.apace100.apoli.access;

import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public interface BlockStateCollisionShapeAccess {
    VoxelShape apoli$getOriginalCollisionShape(BlockView world, BlockPos pos, ShapeContext context);
}
