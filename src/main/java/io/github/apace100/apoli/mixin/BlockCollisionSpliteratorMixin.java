package io.github.apace100.apoli.mixin;

import com.google.common.collect.AbstractIterator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.access.BlockStateCollisionShapeAccessor;
import io.github.apace100.apoli.access.Identifiable;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BlockCollisionSpliterator.class)
public abstract class BlockCollisionSpliteratorMixin<T> extends AbstractIterator<T> implements Identifiable {

    @Unique
    Identifier apoli$id;

    @Override
    public Optional<Identifier> apoli$getId() {
        return Optional.ofNullable(apoli$id);
    }

    @Override
    public void apoli$setId(Identifier id) {
        this.apoli$id = id;
    }

    @WrapOperation(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;"))
    private VoxelShape apoli$overrideCollisionShapeQuery(BlockState instance, BlockView world, BlockPos pos, ShapeContext context, Operation<VoxelShape> original) {
        return this.apoli$getId().isEmpty() ? original.call(instance, world, pos, context) : ((BlockStateCollisionShapeAccessor) instance).apoli$getCollisionShape(world, pos, context);
    }

}
