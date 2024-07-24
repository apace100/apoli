package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.access.BlockStateCollisionShapeAccess;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.power.PreventBlockSelectionPower;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin extends State<Block, BlockState> implements BlockStateCollisionShapeAccess {

    @Shadow
    public abstract Block getBlock();

    @Shadow
    public abstract VoxelShape getCollisionShape(BlockView world, BlockPos pos, ShapeContext context);

    @Unique
    private boolean apoli$queryOriginal = false;

    protected AbstractBlockStateMixin(Block owner, Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> codec) {
        super(owner, propertyMap, codec);
    }

    @ModifyReturnValue(method = "getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("RETURN"))
    private VoxelShape apoli$preventBlockSelection(VoxelShape original, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        return PreventBlockSelectionPower.doesPrevent(context, blockPos)
            ? VoxelShapes.empty()
            : original;
    }

    @ModifyReturnValue(method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("RETURN"))
    private VoxelShape apoli$phaseThroughBlocks(VoxelShape original, BlockView blockView, BlockPos blockPos, ShapeContext context) {
        return !apoli$queryOriginal && PhasingPower.shouldPhase(context, original, blockPos)
            ? VoxelShapes.empty()
            : original;
    }

    @WrapWithCondition(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"))
    private boolean apoli$preventOnEntityCollisionCallWhenPhasing(Block instance, BlockState state, World world, BlockPos blockPos, Entity entity) {
        return !PowerHolderComponent.hasPower(entity, PhasingPower.class, p -> p.doesApply(blockPos));
    }

    @Override
    public VoxelShape apoli$getOriginalCollisionShape(BlockView world, BlockPos pos, ShapeContext context) {

        this.apoli$queryOriginal = true;
        VoxelShape originalShape = this.getCollisionShape(world, pos, context);

        this.apoli$queryOriginal = false;
        return originalShape;

    }

}
