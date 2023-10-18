package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyInsomniaTicksPower;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @ModifyExpressionValue(method = "spawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/stat/ServerStatHandler;getStat(Lnet/minecraft/stat/Stat;)I"))
    private int apoli$modifyEffectiveTimeSinceRestValue(int original, ServerWorld world, boolean spawnMonsters, boolean spawnAnimals, @Local ServerPlayerEntity player) {
        return (int) PowerHolderComponent.modify(player, ModifyInsomniaTicksPower.class, original);
    }

}
