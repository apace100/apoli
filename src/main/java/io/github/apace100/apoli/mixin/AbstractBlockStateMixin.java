package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PhasingPower;
import io.github.apace100.apoli.power.PreventBlockSelectionPower;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("deprecation")
@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {

    @Shadow
    public abstract Block getBlock();

    @Shadow
    protected abstract BlockState asBlockState();

    @Shadow
    public abstract VoxelShape getOutlineShape(BlockView world, BlockPos pos);

    @Inject(method = "getOutlineShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void preventBlockSelection(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if(context instanceof EntityShapeContext) {
            if(((EntityShapeContext)context).getEntity() != null) {
                Entity entity = ((EntityShapeContext)context).getEntity();
                if(PowerHolderComponent.getPowers(entity, PreventBlockSelectionPower.class).stream().anyMatch(p -> p.doesPrevent(entity.getWorld(), pos))) {
                    cir.setReturnValue(VoxelShapes.empty());
                }
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", cancellable = true)
    private void phaseThroughBlocks(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> info) {
        VoxelShape blockShape = info.getReturnValue();
        if(!blockShape.isEmpty() && context instanceof EntityShapeContext) {
            EntityShapeContext esc = (EntityShapeContext)context;
            if(esc.getEntity() != null) {
                Entity entity = esc.getEntity();
                boolean isAbove = isAbove(entity, blockShape, pos, false);
                for (PhasingPower phasingPower : PowerHolderComponent.getPowers(entity, PhasingPower.class)) {
                    if(!isAbove || phasingPower.shouldPhaseDown(entity)) {
                        if(phasingPower.doesApply(pos)) {
                            info.setReturnValue(VoxelShapes.empty());
                        }
                    }
                }
            }
        }
    }

    @Unique
    private boolean isAbove(Entity entity, VoxelShape shape, BlockPos pos, boolean defaultValue) {
        return entity.getY() > (double)pos.getY() + shape.getMax(Direction.Axis.Y) - (entity.isOnGround() ? 8.05/16.0 : 0.0015);
    }

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void preventCollisionWhenPhasing(World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        for (PhasingPower phasingPower : PowerHolderComponent.getPowers(entity, PhasingPower.class)) {
            if(phasingPower.doesApply(pos)) {
                ci.cancel();
            }
        }
    }
}
