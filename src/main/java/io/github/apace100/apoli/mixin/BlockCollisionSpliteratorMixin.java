package io.github.apace100.apoli.mixin;

import com.google.common.collect.AbstractIterator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.access.BlockStateCollisionShapeAccess;
import io.github.apace100.apoli.access.BlockCollisionSpliteratorAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockCollisionSpliterator.class)
public abstract class BlockCollisionSpliteratorMixin<T> extends AbstractIterator<T> implements BlockCollisionSpliteratorAccess {

    @Unique
    boolean apoli$getOriginalShapes;

    @Override
    public boolean apoli$shouldGetOriginalShapes() {
        return apoli$getOriginalShapes;
    }

    @Override
    public void apoli$setGetOriginalShapes(boolean getOriginalShapes) {
        this.apoli$getOriginalShapes = getOriginalShapes;
    }

    @WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape apoli$overrideCollisionShapeQuery(BlockState state, BlockView world, BlockPos pos, ShapeContext context, Operation<VoxelShape> original) {
        return state instanceof BlockStateCollisionShapeAccess shapeAccess && this.apoli$shouldGetOriginalShapes()
            ? shapeAccess.apoli$getOriginalCollisionShape(world, pos, context)
            : original.call(state, world, pos, context);
    }

}
