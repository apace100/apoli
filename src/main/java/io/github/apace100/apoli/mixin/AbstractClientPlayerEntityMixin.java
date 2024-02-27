package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyFovPower;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {

    private AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @ModifyExpressionValue(method = "getFovMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isUsingSpyglass()Z"))
    private boolean apoli$delegateSpyglassFovMultiplier(boolean original) {
        return !PowerHolderComponent.hasPower(this, ModifyFovPower.class) && original;
    }

    @WrapOperation(method = "getFovMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float apoli$modifyFov(float delta, float start, float end, Operation<Float> original) {

        List<ModifyFovPower> mfps = PowerHolderComponent.getPowers(this, ModifyFovPower.class);
        if (mfps.isEmpty()) {
            return original.call(delta, start, end);
        }

        boolean affectedByFovEffectScale = mfps
            .stream()
            .anyMatch(ModifyFovPower::isAffectedByFovEffectScale);

        float newEnd = PowerHolderComponent.modify(this, ModifyFovPower.class, end);
        float newDelta = affectedByFovEffectScale ? delta : start;

        return MathHelper.lerp(newDelta, start, newEnd);

    }

}
