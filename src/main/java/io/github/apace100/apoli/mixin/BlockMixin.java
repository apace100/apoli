package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyBouncinessPower;
import io.github.apace100.apoli.util.BouncinessMultiplierRegistry;
import io.github.apace100.calio.ClassUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {

    @Unique private World apoli$landedUponWorld;
    @Unique private BlockPos apoli$landedUponBlockPos;
    @Unique private Entity apoli$landedUponEntity;

    @Inject(method = "onLandedUpon", at = @At(value = "HEAD"))
    private void captureFallDamageCancelParams(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        this.apoli$landedUponWorld = world;
        this.apoli$landedUponBlockPos = pos;
        this.apoli$landedUponEntity = entity;
    }

    @ModifyArg(method = "onLandedUpon", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;handleFallDamage(FFLnet/minecraft/entity/damage/DamageSource;)Z"), index = 1)
    private float cancelFallDamage(float multiplier) {
        if (ModifyBouncinessPower.shouldCancelFallDamage(apoli$landedUponEntity, new CachedBlockPosition(apoli$landedUponWorld, apoli$landedUponBlockPos, false))) {
            return 0.0F;
        }
        return multiplier;
    }

    @Unique private Entity apoli$onLandEntity;

    @Inject(method = "onEntityLand", at = @At(value = "HEAD"))
    private void captureBouncinessParams(BlockView world, Entity entity, CallbackInfo ci) {
        apoli$onLandEntity = entity;
    }


    @ModifyArg(method = "onEntityLand", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private Vec3d modifyBouncinessY(Vec3d original) {
        CachedBlockPosition cbp = new CachedBlockPosition(apoli$onLandEntity.getWorld(), apoli$onLandEntity.getSteppingPos(), false);
        if (apoli$onLandEntity.getVelocity().getY() < 0.0 && PowerHolderComponent.getPowers(apoli$onLandEntity, ModifyBouncinessPower.class).stream().anyMatch(p -> p.doesApply(cbp))) {
            return new Vec3d(original.getX(), MathHelper.abs(ModifyBouncinessPower.modify(apoli$onLandEntity, BouncinessMultiplierRegistry.getValue(ClassUtil.castClass(this.getClass())), cbp)), original.getZ());
        }
        return original;
    }
}
