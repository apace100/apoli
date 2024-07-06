package io.github.apace100.apoli.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public interface ServerPlayerEntityAccessor {

    @Invoker
    static Optional<ServerPlayerEntity.RespawnPos> callFindRespawnPosition(ServerWorld world, BlockPos pos, float spawnAngle, boolean spawnForced, boolean alive) {
        throw new AssertionError();
    }

}
