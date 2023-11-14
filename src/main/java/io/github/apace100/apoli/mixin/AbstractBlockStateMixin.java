package io.github.apace100.apoli.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.access.BlockStateCollisionShapeAccessor;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.power.PreventBlockSelectionPower;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin extends State<Block, BlockState> implements BlockStateCollisionShapeAccessor {

    @Shadow public abstract Block getBlock();

    @Shadow protected abstract BlockState asBlockState();

    protected AbstractBlockStateMixin(Block owner, ImmutableMap<Property<?>, Comparable<?>> entries, MapCodec<BlockState> codec) {
        super(owner, entries, codec);
    }

    @ModifyReturnValue(method = "getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("RETURN"))
    private VoxelShape apoli$preventBlockSelection(VoxelShape original, BlockView blockView, BlockPos blockPos, ShapeContext shapeContext) {

        Entity entity;
        if (!(shapeContext instanceof EntityShapeContext esc) || (entity = esc.getEntity()) == null) {
            return original;
        }

        return PowerHolderComponent.hasPower(entity, PreventBlockSelectionPower.class, p -> p.doesPrevent(entity.getWorld(), blockPos)) ? VoxelShapes.empty() : original;

    }

    @ModifyReturnValue(method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("RETURN"))
    private VoxelShape apoli$phaseThroughBlocks(VoxelShape original, BlockView blockView, BlockPos blockPos, ShapeContext shapeContext) {

        Entity entity;
        if (original.isEmpty() || !(shapeContext instanceof EntityShapeContext esc) || (entity = esc.getEntity()) == null) {
            return original;
        }

        Predicate<PhasingPower> phasingApplies = p -> (!apoli$isAbove(entity, original, blockPos) || p.shouldPhaseDown(entity)) && p.doesApply(blockPos);
        return PowerHolderComponent.hasPower(entity, PhasingPower.class, phasingApplies) ? VoxelShapes.empty() : original;

    }

    @Unique
    private boolean apoli$isAbove(Entity entity, VoxelShape shape, BlockPos pos) {
        return entity.getY() > (double) pos.getY() + shape.getMax(Direction.Axis.Y) - (entity.isOnGround() ? 8.05 / 16.0 : 0.0015);
    }

    @WrapWithCondition(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"))
    private boolean apoli$preventOnEntityCollisionCallWhenPhasing(Block instance, BlockState state, World world, BlockPos blockPos, Entity entity) {
        return !PowerHolderComponent.hasPower(entity, PhasingPower.class, p -> p.doesApply(blockPos));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape apoli$getCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {
        return this.getBlock().getCollisionShape(this.asBlockState(), world, pos, context);
    }

}
