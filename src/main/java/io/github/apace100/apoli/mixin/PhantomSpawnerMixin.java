package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyInsomniaTicksPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Random;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Unique
    private PlayerEntity apoli$CachedPlayer;

    @Inject(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/dimension/DimensionType;hasSkyLight()Z", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void cachePlayerEntity(ServerWorld world, boolean spawnMonsters, boolean spawnAnimals, CallbackInfoReturnable<Integer> cir, Random random, int i, Iterator var6, PlayerEntity playerEntity, BlockPos blockPos) {
        apoli$CachedPlayer = playerEntity;
    }

    @ModifyVariable(method = "spawn", at = @At(value = "INVOKE",target = "Ljava/util/Random;nextInt(I)I", ordinal = 1, shift = At.Shift.BEFORE), ordinal = 1)
    private int modifyTicks(int original) {
        return (int)PowerHolderComponent.modify(apoli$CachedPlayer, ModifyInsomniaTicksPower.class, original);
    }
}
